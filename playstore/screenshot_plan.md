# Tiny Next Screenshot Plan

Goal: show the actual Android MVP workflow, not marketing art.

## Required Screens

1. Main task picker
   - Show a short task list and a clear selected next task.
   - Use realistic but generic tasks such as "Reply to one email" and "Clear desk for five minutes."

2. Add or edit task
   - Show the smallest input flow.
   - Avoid personal data in sample text.

3. Progress or history
   - Show completed tiny tasks or simple stats if the MVP includes them.
   - If stats are not implemented, skip this screenshot instead of staging fake UI.

4. Premium screen
   - Show the one-time remove-ads offer and restore action.
   - Use the Play Billing test product price from an internal test build.

5. Free app with banner placement
   - Show where a banner appears for non-premium users.
   - Use Google's test ad label or a debug build, never a live ad in screenshots.

## Capture Rules

- Capture from the real app build.
- Use neutral sample tasks only.
- Do not include emails, names, addresses, or calendar details.
- Prefer Pixel 8 phone-sized screenshots unless Play Console requires additional form factors.
- Re-capture after any layout change that affects the first screen, premium screen, or ad placement.
