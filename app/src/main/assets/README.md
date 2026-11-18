# Shree Gurudev Daat Clinic

A mobile-friendly Progressive Web App (PWA) for patients and a separate doctor/staff web portal. Firebase provides Authentication, Cloud Firestore and Cloud Storage.

## What is included

- Separate patient experience and clinic portal, selected from the authenticated account role.
- Email/password sign-in, patient self-registration and password reset using Firebase Authentication.
- Patient profile and medical-history forms, clinic appointment requests and doctor scheduling/status controls.
- Patient-scoped record views, treatment records, per-treatment consent acknowledgements, payment tracking and outside X-ray/document uploads.
- Firebase Firestore and Storage security rules, installable PWA manifest, app icon and service worker.
- Responsive and dark-mode styling.

This is a working starter application, not a medical-records production certification. The clinic must approve consent wording/translations, retention policy, payment processes and clinical workflows. Do not use the demo as a substitute for clinical judgment.

## Firebase setup

1. Create a Firebase project at [Firebase Console](https://console.firebase.google.com/), add a Web app, and copy its web configuration into `firebase-config.js`.
2. In **Authentication → Sign-in method**, enable **Email/Password**.
3. Create a **Cloud Firestore** database and a **Storage** bucket.
4. Deploy `firestore.rules` and `storage.rules` from the Firebase console (or Firebase CLI). Never deploy permissive test rules.
5. Create doctor/staff accounts in Authentication, then create `users/{AUTH_UID}` documents in Firestore with `role: "doctor"` or `role: "staff"` and the matching `uid`. Assign these roles only to authorized clinic accounts. New public sign-ups are restricted to patient role by the rules.
6. Host the project over HTTPS. Firebase Hosting is recommended. A PWA service worker does not work from a `file://` URL or an insecure non-localhost origin.

### Example Firebase CLI deployment

Install the Firebase CLI, run `firebase login`, then from the repository directory:

```sh
firebase init hosting firestore storage
# Choose the existing project; set the public directory to the repository root for this no-build app.
firebase deploy --only hosting,firestore:rules,storage
```

When prompted about a single-page app, choose yes so app routes fall back to `index.html`. The rules files should be selected as `firestore.rules` and `storage.rules`. The Firebase client configuration is not a server credential, but Firestore and Storage rules are essential and must be deployed before entering real patient data.

## Run locally

After replacing the Firebase config and deploying rules, serve the repository over localhost, for example:

```sh
python -m http.server 8080
```

Open `http://localhost:8080`. Firebase Auth authorized domains must include the host you use. For development, localhost is normally accepted.

## Install as an app

Open the hosted HTTPS site on a supported mobile browser. Use **Install app / Add to Home Screen** from the browser menu. On desktop Chrome/Edge, use the install icon in the address bar. The service worker caches the app shell; authentication and patient data still require a network connection.

## Data model overview

- `users/{uid}`: account role and email.
- `patients/{patientId}`: profile, guardian contact and medical history; `uid` links the patient account.
- `appointments/{appointmentId}`: patient-linked appointment request/schedule and status.
- `treatments/{treatmentId}`: diagnosis, procedure, tooth/area and status.
- `images/{imageId}`: file metadata, date taken, date uploaded, source and review status. Binary files live in Storage under `patients/{patientId}/`.
- `consents/{consentId}`: treatment link, language, text/version, signer and acknowledgement.
- `payments/{paymentId}`: amount, description and clinic-updated status.

## Important before production

The included example uses client-side Firebase access. Configure Firebase App Check, least-privilege rules, MFA for clinic staff, backups, audit/retention policies and monitoring. Review applicable privacy/health-data requirements with the clinic. Verify that every patient-visible query is scoped to the signed-in patient. Manual transfer receipts must never automatically count as payment received. Use an approved payment provider for verified online payments. Appointment reminders, push delivery, native push notifications, prescription management, full consent version approvals and report export are not yet integrated.
