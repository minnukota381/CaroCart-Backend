#!/bin/bash

set -e  # Exit on any error

export AWS_PAGER=""


# AWS and ECS Configuration
AWS_ACCOUNT_ID=815931044195
REGION=ap-south-1
CLUSTER_NAME="carocart-cluster"
ECR_URL="$AWS_ACCOUNT_ID.dkr.ecr.$REGION.amazonaws.com"

# ECS execution role ARN
EXECUTION_ROLE_ARN="arn:aws:iam::$AWS_ACCOUNT_ID:role/ecsTaskExecutionRole"

# Microservice definitions: DIR:ECR_REPO:TASK_DEF:ECS_SERVICE
SERVICES=(
  "CaroCartAddress:carocart-address:carocart-address:carocart-address-service-csg9erxv"
  "CaroCartAPIGateway:carocart-apigateway:carocart-apigateway:carocart-apigateway-service-pj91zvwr"
  "CaroCartAuthentication:carocart-authentication:carocart-authentication-task:carocart-authentication-task-service-m79r5xga"
  "CartService:carocart-cartservice:carocart-cartservice:carocart-cartservice-service-18l7tj3j"
  "CaroCartServer:carocart-eureka-server:carocart-eureka-task:carocart-eureka-task-service-wfes0zrh"
  "CaroCartNotifications:carocart-notifications:carocart-notifications:carocart-notifications-service-o1la5m4m"
  "OrderService:carocart-orderservice:carocart-orderservice:carocart-orderservice-service-8s6g5lkf"
  "PaymentService:carocart-paymentservice:carocart-paymentservice:carocart-paymentservice-service-f79gdmkp"
  "CaroCartProductService:carocart-productservice:carocart-productservice:carocart-productservice-service-oadxvv8p"
)

# Step 1: Maven Build
echo "🔧 Running Maven clean + package..."
mvn clean package -DskipTests
echo "✅ Maven build complete."

# Step 2: Docker login
echo "🔐 Logging in to AWS ECR..."
aws ecr get-login-password --region "$REGION" | docker login --username AWS --password-stdin "$ECR_URL"

# Step 3: Loop through each service
for entry in "${SERVICES[@]}"; do
  IFS=":" read DIR REPO TASK_DEF SERVICE <<< "$entry"
  IMAGE="$ECR_URL/$REPO:latest"

  echo "🚀 Building Docker image for $DIR..."
  cd "$DIR" || { echo "❌ Directory $DIR not found"; exit 1; }
  docker build -t "$IMAGE" .
  docker push "$IMAGE"
  cd ..

  echo "📦 Updating ECS task definition for $TASK_DEF..."

  # Fetch current task definition
  TASK_DEF_JSON=$(aws ecs describe-task-definition --task-definition "$TASK_DEF" --region "$REGION" --output json)

  # Clean and update task definition (removing nulls, injecting new image)
  UPDATED_JSON=$(echo "$TASK_DEF_JSON" | jq --arg IMAGE "$IMAGE" --arg NAME "$REPO" '
    .taskDefinition |
    {
      family,
      executionRoleArn,
      networkMode,
      requiresCompatibilities,
      cpu,
      memory,
      containerDefinitions,
      volumes
    } |
    .containerDefinitions |= map(if .name == $NAME then .image = $IMAGE else . end)
  ')

  # Register updated task definition
  REVISION=$(aws ecs register-task-definition \
    --cli-input-json "$(echo "$UPDATED_JSON")" \
    --region "$REGION" \
    --query 'taskDefinition.revision' \
    --output text)

  echo "✅ Registered new revision: $TASK_DEF:$REVISION"

  # Update ECS service to use new task definition
  echo "📢 Updating ECS service $SERVICE..."
  aws ecs update-service \
    --cluster "$CLUSTER_NAME" \
    --service "$SERVICE" \
    --task-definition "$TASK_DEF:$REVISION" \
    --region "$REGION" \
    --force-new-deployment

  echo "✅ Updated $SERVICE to revision $REVISION"
  echo "---------------------------------------------"
done

echo "🎉 All microservices built, pushed, and deployed successfully!"
