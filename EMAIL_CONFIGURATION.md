# Email Configuration Guide

## Overview
The application now uses real email sending via Spring Boot Starter Mail with JavaMailSender. All email operations are asynchronous and non-blocking.

## Configuration

### Application Properties
Configure the following properties in `application.properties` or via environment variables:

```properties
# SMTP Server Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password

# Mail Properties
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true

# Sender Information
app.mail.from=noreply@shopjoy.com
app.mail.from-name=ShopJoy E-Commerce
```

### Environment Variables
For production, use environment variables:
- `MAIL_HOST` - SMTP server hostname
- `MAIL_PORT` - SMTP server port (587 for TLS, 465 for SSL)
- `MAIL_USERNAME` - Email account username
- `MAIL_PASSWORD` - Email account password or app password
- `MAIL_FROM` - Sender email address
- `MAIL_FROM_NAME` - Sender display name

## Gmail Configuration

### Using Gmail SMTP
1. **Enable 2-Factor Authentication** on your Gmail account
2. **Generate App Password**:
   - Go to Google Account Settings → Security
   - Under "How you sign in to Google", select "App passwords"
   - Generate a new app password for "Mail"
   - Use this password in `MAIL_PASSWORD`

### Gmail Settings
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-gmail@gmail.com
spring.mail.password=your-16-char-app-password
```

## Other Email Providers

### Outlook/Office365
```properties
spring.mail.host=smtp.office365.com
spring.mail.port=587
```

### SendGrid
```properties
spring.mail.host=smtp.sendgrid.net
spring.mail.port=587
spring.mail.username=apikey
spring.mail.password=your-sendgrid-api-key
```

### Amazon SES
```properties
spring.mail.host=email-smtp.us-east-1.amazonaws.com
spring.mail.port=587
spring.mail.username=your-ses-smtp-username
spring.mail.password=your-ses-smtp-password
```

## Email Templates

The service sends HTML emails for:
- **Order Confirmation** - Sent when order is created
- **Order Cancellation** - Sent when order is cancelled
- **Payment Confirmation** - Sent when payment is processed

All templates include:
- Professional HTML styling
- Order/payment details
- Company branding
- Customer support information

## Testing

### Local Development
For local testing without a real SMTP server, you can:

1. **Use MailHog** (recommended for development):
```properties
spring.mail.host=localhost
spring.mail.port=1025
```

2. **Use Gmail** with app password (see above)

3. **Mock the JavaMailSender** in tests

### Verify Email Sending
Check application logs for:
```
INFO  - Order confirmation email sent successfully in XXXms to user@email.com
```

## Troubleshooting

### Common Issues

**Authentication Failed**
- Verify username and password are correct
- For Gmail, ensure you're using an App Password, not your regular password
- Check if 2FA is enabled

**Connection Timeout**
- Verify SMTP host and port are correct
- Check firewall settings
- Ensure `starttls.enable=true` for port 587

**Email Not Received**
- Check spam/junk folder
- Verify sender email is not blacklisted
- Check SMTP server logs
- Verify recipient email is correct

### Debug Logging
Enable debug logging for mail:
```properties
logging.level.org.springframework.mail=DEBUG
logging.level.com.shopjoy.service.impl.EmailServiceImpl=DEBUG
```

## Async Execution

All email operations use `@Async("appTaskExecutor")` and return `CompletableFuture<Void>`:
- Non-blocking - doesn't delay order creation/payment
- Fire-and-forget - failures are logged but don't affect transactions
- Timing metrics - execution time logged for monitoring

## Production Recommendations

1. **Use dedicated SMTP service** (SendGrid, Amazon SES, Mailgun)
2. **Monitor email delivery** rates and failures
3. **Set up SPF/DKIM/DMARC** records for better deliverability
4. **Implement retry logic** for failed sends (future enhancement)
5. **Store email history** in database for audit trail (future enhancement)
6. **Use environment variables** for sensitive credentials
7. **Never commit** SMTP credentials to version control

## Security Best Practices

- ✅ Use TLS/SSL for SMTP connections
- ✅ Store credentials as environment variables
- ✅ Use app passwords instead of account passwords
- ✅ Limit SMTP user permissions
- ✅ Rotate credentials regularly
- ✅ Monitor for unauthorized email sends

