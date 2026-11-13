const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_gm_123";
const BOB_UID = "bob_gm_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validLeadPayload(uid, overrides = {}) {
  const now = new Date();
  return {
    userId: uid,
    name: "Tariq Mahmood",
    company: "Capital Builders Islamabad",
    designation: "CEO",
    city: "Islamabad",
    industry: "Property Developers",
    contactPhone: "+923001234567",
    email: "info@capitalbuilders.pk",
    website: "https://capitalbuilders.pk",
    linkedInUrl: "https://linkedin.com/in/tariq-mahmood",
    source: "Public Search",
    notes: "Interested in family takaful group cover.",
    permissionStatus: "VERIFIED_PUBLIC",
    status: "NEW",
    followUpDate: "2026-11-20",
    createdAt: now,
    updatedAt: now,
    ...overrides,
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read or write leads", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("leads").get()
  );
  await assertFails(
    unauthDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("leads")
      .doc("lead_1")
      .set(validLeadPayload(ALICE_UID))
  );
});

test("Owner can create, read, list, and update own lead", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const leadRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("leads")
    .doc("lead_1");

  await assertSucceeds(leadRef.set(validLeadPayload(ALICE_UID)));
  await assertSucceeds(leadRef.get());
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("leads").get()
  );
  await assertSucceeds(
    leadRef.update({
      status: "CONTACTED",
      followUpDate: "2026-11-25",
      updatedAt: new Date(),
    })
  );
});

test("Cross-user access is strictly denied for leads", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context
      .firestore()
      .collection("users")
      .doc(BOB_UID)
      .collection("leads")
      .doc("bob_lead")
      .set(validLeadPayload(BOB_UID));
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb
      .collection("users")
      .doc(BOB_UID)
      .collection("leads")
      .doc("bob_lead")
      .get()
  );
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).collection("leads").get()
  );
  await assertFails(
    aliceDb
      .collection("users")
      .doc(BOB_UID)
      .collection("leads")
      .doc("hack_lead")
      .set(validLeadPayload(BOB_UID))
  );
});

test("Shadow update with ghost field or identity spoofing is rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const leadRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("leads")
    .doc("lead_2");

  // Ghost field on create
  await assertFails(
    leadRef.set(validLeadPayload(ALICE_UID, { isAdmin: true }))
  );

  // Spoofed userId on create
  await assertFails(
    leadRef.set(validLeadPayload(BOB_UID))
  );

  // Valid create, then immutable createdAt tamper on update
  await assertSucceeds(leadRef.set(validLeadPayload(ALICE_UID)));
  await assertFails(
    leadRef.update({
      createdAt: new Date(Date.now() - 100000),
      updatedAt: new Date(),
    })
  );
});

test("Terminal ARCHIVED status locks further lead updates", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const leadRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("leads")
    .doc("lead_archived");

  await assertSucceeds(
    leadRef.set(validLeadPayload(ALICE_UID, { status: "ARCHIVED" }))
  );
  await assertFails(
    leadRef.update({
      status: "NEW",
      updatedAt: new Date(),
    })
  );
});

test("Contact history requires existing parent lead and rejects updates", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const leadRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("leads")
    .doc("lead_hist");
  const histRef = leadRef.collection("history").doc("entry_1");

  // Fails when parent lead does not exist
  await assertFails(
    histRef.set({
      userId: ALICE_UID,
      leadId: "lead_hist",
      channel: "WHATSAPP",
      summary: "Sent intro after review.",
      outcome: "Awaiting reply",
      createdAt: new Date(),
    })
  );

  await assertSucceeds(leadRef.set(validLeadPayload(ALICE_UID)));
  await assertSucceeds(
    histRef.set({
      userId: ALICE_UID,
      leadId: "lead_hist",
      channel: "WHATSAPP",
      summary: "Sent intro after review.",
      outcome: "Awaiting reply",
      createdAt: new Date(),
    })
  );

  // Updates to history log are forbidden
  await assertFails(
    histRef.update({ summary: "Tampered summary" })
  );
});

test("Product verification notes enforce valid official product IDs", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date();
  const validNoteRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("productNotes")
    .doc("golden_path");

  await assertSucceeds(
    validNoteRef.set({
      userId: ALICE_UID,
      productId: "golden_path",
      verifiedSummary: "Verified from askarilife.com brochure.",
      verifiedEligibility: "Verified eligibility criteria.",
      verifiedSourceDate: "2026-11-13",
      isVerifiedByUser: true,
      createdAt: now,
      updatedAt: now,
    })
  );

  const invalidNoteRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("productNotes")
    .doc("unknown_plan");

  await assertFails(
    invalidNoteRef.set({
      userId: ALICE_UID,
      productId: "unknown_plan",
      isVerifiedByUser: false,
      createdAt: now,
      updatedAt: now,
    })
  );
});

test("Prospects collection enforces per-user ownership and schema", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date();

  const aliceProspectRef = aliceDb.collection("prospects").doc("prospect_1");
  const validPayload = {
    userId: ALICE_UID,
    name: "Usman Farooq",
    contact: "+923001119988",
    status: "Interested",
    notes: "Met at Blue Area Islamabad.",
    createdAt: now,
    updatedAt: now,
  };

  // Unauthenticated denied
  await assertFails(unauthDb.collection("prospects").doc("p_unauth").set(validPayload));
  await assertFails(unauthDb.collection("prospects").get());

  // Spoofed userId denied
  await assertFails(
    aliceDb.collection("prospects").doc("p_spoof").set({ ...validPayload, userId: BOB_UID })
  );

  // Ghost field denied
  await assertFails(
    aliceDb.collection("prospects").doc("p_ghost").set({ ...validPayload, adminFlag: true })
  );

  // Owner create, get, scoped query, update, and delete succeed
  await assertSucceeds(aliceProspectRef.set(validPayload));
  await assertSucceeds(aliceProspectRef.get());
  await assertSucceeds(
    aliceDb.collection("prospects").where("userId", "==", ALICE_UID).get()
  );
  await assertSucceeds(
    aliceProspectRef.update({ status: "Contacted", updatedAt: new Date() })
  );

  // Cross-user read, query, update, and delete denied
  await assertFails(bobDb.collection("prospects").doc("prospect_1").get());
  await assertFails(
    bobDb.collection("prospects").where("userId", "==", ALICE_UID).get()
  );
  await assertFails(
    bobDb.collection("prospects").doc("prospect_1").update({ status: "Hacked", updatedAt: new Date() })
  );
  await assertFails(bobDb.collection("prospects").doc("prospect_1").delete());

  // Owner delete succeeds
  await assertSucceeds(aliceProspectRef.delete());
});

test("GM monthly targets enforce owner isolation and positive integer bounds", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date();
  const ref = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("gmSettings")
    .doc("monthly_targets");

  const validTargets = {
    userId: ALICE_UID,
    targetMonth: "2026-11",
    salesTarget: 8,
    outreachTarget: 30,
    referralTarget: 12,
    createdAt: now,
    updatedAt: now,
  };

  await assertSucceeds(ref.set(validTargets));
  await assertSucceeds(ref.get());
  await assertSucceeds(
    ref.update({ salesTarget: 10, updatedAt: new Date() })
  );

  // Invalid negative target denied
  await assertFails(ref.update({ salesTarget: 0, updatedAt: new Date() }));

  // Arbitrary setting ID denied
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("gmSettings")
      .doc("other_doc")
      .set(validTargets)
  );

  // Cross-user read/write denied
  await assertFails(
    bobDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("gmSettings")
      .doc("monthly_targets")
      .get()
  );
  await assertFails(
    bobDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("gmSettings")
      .doc("monthly_targets")
      .update({ salesTarget: 99, updatedAt: new Date() })
  );
});
