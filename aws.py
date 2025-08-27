import boto3
import json

def lambda_handler(event, context):
    # Parse incoming IPv6 from POST body
    body = json.loads(event.get("body", "{}"))
    ipv6 = body.get("ipv6")
    if not ipv6:
        return {"statusCode": 400, "body": "No IPv6 provided"}

    # Route 53 update
    client = boto3.client('route53')
    HOSTED_ZONE_ID = "YOUR_ZONE_ID"
    RECORD_NAME = "sub.example.com"

    response = client.change_resource_record_sets(
        HostedZoneId=HOSTED_ZONE_ID,
        ChangeBatch={
            "Comment": "Update AAAA record via Lambda",
            "Changes": [
                {
                    "Action": "UPSERT",
                    "ResourceRecordSet": {
                        "Name": RECORD_NAME,
                        "Type": "AAAA",
                        "TTL": 60,
                        "ResourceRecords": [{"Value": ipv6}]
                    }
                }
            ]
        }
    )

    return {"statusCode": 200, "body": json.dumps({"message": "IPv6 updated", "ipv6": ipv6})}
