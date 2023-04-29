aws --endpoint-url=http://localhost:4566 sqs create-queue --queue-name=persons-queue
aws --endpoint-url=http://localhost:4566 sns create-topic --name persons-sns-topic
aws --endpoint-url=http://localhost:4566 sns subscribe \
  --topic-arn=arn:aws:sns:us-east-1:000000000000:persons-sns-topic \
  --protocol=sqs \
  --notification-endpoint=arn:aws:sqs:us-east-1:000000000000:persons-queue
#  --attributes='{"FilterPolicy":"{\"messageName\": [\"APPLICATION_SUBMITTED\",\"ACCOUNT_ACTIVATED\",\"ACCOUNT_PAID_OFF\",\"ACCOUNT_PAST_DUE\",\"ACCOUNT_CANCELLED\",\"APPLICATION_SUBMITTED_V2\",\"LOAN_DISBURSED_V2\",\"ACCOUNT_STATUS_UPDATED\",\"DISBURSEMENT_STATUS_UPDATED\",\"REPAYMENT_ACCOUNT_STATUS_UPDATED\",\"APPLICATION_STATUS_UPDATED\", \"PAYMENT_REVERSAL\"]}"}'
