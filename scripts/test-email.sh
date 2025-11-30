#!/bin/bash

# Script to test sending emails through GreenMail
# This script sends a test email using the 'curl' command

echo "Sending test email to GreenMail..."

# Email data
FROM="test@cvix.local"
TO="recipient@example.com"
SUBJECT="Test email from script"
BODY="This is a test email sent from the test-email.sh script"

# Send email using SMTP directly
curl --url smtp://localhost:3025 \
     --mail-from $FROM \
     --mail-rcpt $TO \
     --upload-file - << EOF
From: Test Sender <$FROM>
To: Test Recipient <$TO>
Subject: $SUBJECT
Date: $(date -R)
Content-Type: text/plain; charset=utf-8

$BODY
EOF

# Check the result
if [ $? -eq 0 ]; then
  echo "✅ Email sent successfully"
  echo "You can view the email in the GreenMail web interface: http://localhost:8080"
else
  echo "❌ Error sending the email"
  echo "Make sure GreenMail is running with: docker compose ps greenmail"
fi

echo ""
echo "To test sending emails from Keycloak:"
echo "1. Access the admin console: http://localhost:9080/admin/"
echo "2. Go to Realm Settings → Email"
echo "3. Click on 'Test connection'"
echo ""
echo "To test sending emails from the Spring Boot application:"
echo "1. Start the application with ./gradlew bootRun"
echo "2. Use a feature that sends emails (registration, password recovery, etc.)"
