#!/bin/bash

# CONFIGURATION
CLUSTER="carocart-cluster"
REGION="ap-south-1"
AWS_ACCOUNT_ID="815931044195"

# Format: taskDefinitionName:containerName
SERVICES=(
  "carocart-address:carocart-address"
  "carocart-apigateway:carocart-apigateway"
  "carocart-authentication-task:carocart-authentication"
  "carocart-cartservice:carocart-cartservice"
  "carocart-eureka-task:carocart-eureka-server"
  "carocart-notifications:carocart-notifications"
  "carocart-orderservice:carocart-orderservice"
  "carocart-paymentservice:carocart-paymentservice"
  "carocart-productservice:carocart-productservice"
)

for svc in "${SERVICES[@]}"; do
  IFS=":" read TASK_DEF CONTAINER_NAME <<< "$svc"
  echo "📦 Updating $TASK_DEF ($CONTAINER_NAME)..."

  # Get current task definition
  TASK_DEF_JSON=$(aws ecs describe-task-definition \
    --task-definition "$TASK_DEF" \
    --region "$REGION" \
    --output json)

  if [ $? -ne 0 ]; then
    echo "❌ Error: Failed to describe $TASK_DEF"
    continue
  fi

  # Build new container image reference
  NEW_IMAGE="$AWS_ACCOUNT_ID.dkr.ecr.$REGION.amazonaws.com/$CONTAINER_NAME:latest"

  # Prepare new task definition JSON using jq
  NEW_TASK_DEF_JSON=$(echo "$TASK_DEF_JSON" | jq \
    --arg IMAGE "$NEW_IMAGE" \
    --arg CONTAINER "$CONTAINER_NAME" '
    .taskDefinition |
    {
      family,
      executionRoleArn,
      taskRoleArn,
      networkMode,
      requiresCompatibilities,
      cpu,
      memory,
      containerDefinitions,
      volumes
    } |
    .containerDefinitions |= map(if .name == $CONTAINER then .image = $IMAGE else . end)
  ')

  # Register the updated task definition
  NEW_REVISION=$(aws ecs register-task-definition \
    --cli-input-json "$NEW_TASK_DEF_JSON" \
    --region "$REGION" \
    --query 'taskDefinition.revision' \
    --output text)

  if [ $? -ne 0 ]; then
    echo "❌ Error: Failed to register new revision for $TASK_DEF"
    continue
  fi

  echo "✅ Registered new revision: $TASK_DEF:$NEW_REVISION"

  # Update the ECS service to use the new revision
  SERVICE_NAME="$TASK_DEF"  # Assuming ECS service name == task definition name
  aws ecs update-service \
    --cluster "$CLUSTER" \
    --service "$SERVICE_NAME" \
    --task-definition "$TASK_DEF:$NEW_REVISION" \
    --region "$REGION" \
    --force-new-deployment

  if [ $? -ne 0 ]; then
    echo "❌ Error: Failed to update ECS service $SERVICE_NAME"
    continue
  fi

  echo "🚀 Deployed $TASK_DEF to ECS service $SERVICE_NAME"
  echo "---------------------------------------------"
done

echo "🎉 All services processed."
