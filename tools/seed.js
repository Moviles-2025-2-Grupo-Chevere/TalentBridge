const admin = require("firebase-admin");
const data = require("./data.json"); // Your data file

admin.initializeApp({
  credential: admin.credential.cert(require("./serviceAccountKey.json")),
});

const db = admin.firestore();

async function seed() {
  const batchSize = 500;
  const collectionName = "myCollection";
  const collectionRef = db.collection(collectionName);

  let batch = db.batch();
  let counter = 0;

  for (const item of data) {
    const docRef = item.id
      ? collectionRef.doc(item.id.toString())
      : collectionRef.doc();
    batch.set(docRef, item);
    counter++;

    // Commit every 500 writes
    if (counter % batchSize === 0) {
      await batch.commit();
      console.log(`Committed ${counter} docs...`);
      batch = db.batch();
    }
  }

  // Commit remaining
  if (counter % batchSize !== 0) {
    await batch.commit();
  }

  console.log(`✅ Done! Seeded ${counter} documents.`);
}

seed().catch(console.error);
