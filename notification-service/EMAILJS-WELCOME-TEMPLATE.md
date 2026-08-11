# EmailJS — Welcome Email Template

The welcome email is sent automatically after a customer verifies their email address:

```text
Verify email  ──►  EMAIL_VERIFIED event  ──►  Notification Service  ──►  EmailJS (welcome template)  ──►  Customer
```

EmailJS templates **cannot be created via API** — they are created in the dashboard
(https://dashboard.emailjs.com) and referenced by their auto-generated id
(`template_xxxxxxx`). Follow the steps below once, then set the id in the
Notification Service environment as `EMAILJS_WELCOME_TEMPLATE_ID`.

---

## Steps (2 minutes)

1. Open https://dashboard.emailjs.com and sign in.
2. In the left sidebar click **Email Templates**.
3. Click **Create New Template** (or duplicate the existing `template_w4koj8i` and edit it).
4. In the **Subject** field, paste the subject below.
5. Switch the editor to **Code / HTML** mode and paste the HTML body below.
6. Leave the **From Name / From Email** as configured on your existing service
   (or set `Sewage Alert Hyderabad` / the service's sender address).
7. Click **Save**, then copy the template id (e.g. `template_abc1234`).
8. Set it in the Notification Service environment:
   - `notification-service/.env.local` → `EMAILJS_WELCOME_TEMPLATE_ID=template_abc1234`
   - or export it / add to your IDE run configuration.

> ✅ This project's welcome template is created — id **`template_4zjeir8`** — and is already
> wired via `EMAILJS_WELCOME_TEMPLATE_ID` in `.env.local` and `.env.example`.

---

## Subject

```
Welcome to Sewage Alert Hyderabad, {{name}}!
```

## HTML body

Paste this into the **Code / HTML** editor:

```html
<!doctype html>
<html lang="en">
  <body style="margin: 0; padding: 0; background-color: #f4f6f8; font-family: Arial, Helvetica, sans-serif;">
    <table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="background-color: #f4f6f8; padding: 32px 16px;">
      <tr>
        <td align="center">
          <table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="max-width: 560px; background-color: #ffffff; border-radius: 12px; overflow: hidden;">
            <!-- Header -->
            <tr>
              <td style="background-color: #0e7490; padding: 28px 32px;">
                <p style="margin: 0; color: #ffffff; font-size: 22px; font-weight: bold;">🚨 Sewage Alert Hyderabad</p>
              </td>
            </tr>
            <!-- Body -->
            <tr>
              <td style="padding: 32px;">
                <h1 style="margin: 0 0 12px; color: #0f172a; font-size: 20px;">Welcome aboard, {{name}}!</h1>
                <p style="margin: 0 0 16px; color: #475569; font-size: 15px; line-height: 1.6;">
                  Thank you for verifying your email address. Your account is now active.
                </p>
                <p style="margin: 0 0 16px; color: #475569; font-size: 15px; line-height: 1.6;">
                  With Sewage Alert Hyderabad you can:
                </p>
                <ul style="margin: 0 0 24px; padding-left: 20px; color: #475569; font-size: 15px; line-height: 1.8;">
                  <li>Report sewage and drainage issues with photos</li>
                  <li>Track the live status of your complaints</li>
                  <li>Get notified as authorities pick up and resolve issues</li>
                </ul>
                <!-- CTA button -->
                <table role="presentation" cellpadding="0" cellspacing="0" style="margin-bottom: 24px;">
                  <tr>
                    <td style="border-radius: 8px; background-color: #0e7490;">
                      <a href="{{login_url}}" style="display: inline-block; padding: 12px 24px; color: #ffffff; font-size: 15px; font-weight: bold; text-decoration: none; border-radius: 8px;">
                        Sign in to your account
                      </a>
                    </td>
                  </tr>
                </table>
                <p style="margin: 0 0 24px; color: #94a3b8; font-size: 13px; line-height: 1.5;">
                  If the button does not work, copy this link into your browser:<br/>
                  <a href="{{login_url}}" style="color: #0e7490;">{{login_url}}</a>
                </p>
                <p style="margin: 0 0 8px; color: #475569; font-size: 14px; line-height: 1.6;">
                  Need help? Call our helpline at <strong>040-2345 6789</strong> or email
                  <a href="mailto:support@sewagealert.telangana.gov.in" style="color: #0e7490;">support@sewagealert.telangana.gov.in</a>.
                </p>
                <p style="margin: 0; color: #475569; font-size: 14px; line-height: 1.6;">
                  Regards,<br/><strong>Sewage Alert Hyderabad Team</strong>
                </p>
              </td>
            </tr>
            <!-- Footer -->
            <tr>
              <td style="padding: 16px 32px; background-color: #f8fafc; border-top: 1px solid #e2e8f0;">
                <p style="margin: 0; color: #94a3b8; font-size: 12px;">
                  © {{current_year|default:''}} Sewage Alert Hyderabad · Government of Telangana initiative
                </p>
              </td>
            </tr>
          </table>
        </td>
      </tr>
    </table>
  </body>
</html>
```

> ℹ️ `{{current_year|default:''}}` renders empty if the variable is absent — you can
> delete that line from the footer if you prefer.

---

## Template parameters sent by the backend

The Notification Service sends exactly these parameters (built from the `EMAIL_VERIFIED`
event + `FRONTEND_URL`):

```json
{
  "name": "<customer-name>",
  "email": "<customer-email>",
  "login_url": "http://localhost:5173/login"
}
```

| Variable         | Template placeholder | Notes                                  |
|------------------|----------------------|----------------------------------------|
| Customer name    | `{{name}}`           | Falls back to "there" if missing       |
| Customer email   | `{{email}}`          |                                        |
| Sign-in link     | `{{login_url}}`      | Built from `FRONTEND_URL` (never hardcoded) |

---

## Verification (end-to-end)

1. Register a customer → account created unverified.
2. Verify the email via the link → `GET /api/v1/auth/verify-email` succeeds.
3. The Notification Service consumes `EMAIL_VERIFIED` and sends the welcome email
   (only when `EMAILJS_WELCOME_TEMPLATE_ID` is set — otherwise it logs and skips).
