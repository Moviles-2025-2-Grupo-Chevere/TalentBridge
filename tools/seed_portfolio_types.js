const admin = require("firebase-admin");
const data = require("./portfolio_types.json");

admin.initializeApp({
    credential: admin.credential.cert(
        require("./secrets/talentbridge-dev-21980-firebase-adminsdk-fbsvc-c59216b4a5.json")
    ),
});

const db = admin.firestore();

async function seedPortfolioTypes() {
    const batchSize = 500;
    const collectionName = "portfolioTypes";
    const collectionRef = db.collection(collectionName);

    let batch = db.batch();
    let counter = 0;

    for (const item of data) {
        const docRef = collectionRef.doc();
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

    console.log(`✅ Done! Seeded ${counter} portfolio types.`);
}

seedPortfolioTypes().catch(console.error);