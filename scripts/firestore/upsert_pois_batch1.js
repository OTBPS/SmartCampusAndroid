/**
 * Firestore POI upsert script (safe, non-destructive by default)
 *
 * Usage:
 *   node scripts/firestore/upsert_pois_batch1.js [path-to-json] [--dry-run]
 *
 * Requirements:
 *   1) npm install firebase-admin
 *   2) Set GOOGLE_APPLICATION_CREDENTIALS to a service-account json file path
 */

const fs = require('fs');
const path = require('path');
const admin = require('firebase-admin');

const DEFAULT_JSON_PATH = path.resolve(__dirname, 'nuist_pois_batch1.json');
const POIS_COLLECTION = 'pois';

function readJsonArray(filePath) {
  const raw = fs.readFileSync(filePath, 'utf8').replace(/^\uFEFF/, '');
  const data = JSON.parse(raw);
  if (!Array.isArray(data)) {
    throw new Error('Input JSON must be an array.');
  }
  return data;
}

function validatePoi(poi) {
  const requiredFields = [
    'id',
    'name',
    'category',
    'description',
    'latitude',
    'longitude',
    'imageUrl',
    'building',
    'keywords',
    'popularity',
    'updatedAt'
  ];

  for (const field of requiredFields) {
    if (!(field in poi)) {
      throw new Error(`POI ${poi.id || '<unknown>'} is missing required field: ${field}`);
    }
  }

  if (typeof poi.id !== 'string' || poi.id.trim() === '') {
    throw new Error('POI id must be a non-empty string.');
  }

  if (!Array.isArray(poi.keywords)) {
    throw new Error(`POI ${poi.id} keywords must be an array.`);
  }
}

function toFirestorePayload(poi) {
  const date = new Date(poi.updatedAt);
  const timestamp = Number.isNaN(date.getTime())
    ? admin.firestore.FieldValue.serverTimestamp()
    : admin.firestore.Timestamp.fromDate(date);

  return {
    id: poi.id,
    name: poi.name,
    category: poi.category,
    description: poi.description,
    latitude: poi.latitude,
    longitude: poi.longitude,
    imageUrl: poi.imageUrl,
    building: poi.building,
    keywords: poi.keywords,
    popularity: poi.popularity,
    updatedAt: timestamp
  };
}

async function upsertPois(pois) {
  const db = admin.firestore();
  let created = 0;
  let updated = 0;

  for (const poi of pois) {
    validatePoi(poi);

    const docRef = db.collection(POIS_COLLECTION).doc(poi.id);
    const before = await docRef.get();

    await docRef.set(toFirestorePayload(poi), { merge: true });

    if (before.exists) {
      updated += 1;
    } else {
      created += 1;
    }
  }

  return { created, updated, total: pois.length };
}

async function main() {
  const args = process.argv.slice(2);
  const dryRun = args.includes('--dry-run');
  const inputArg = args.find((arg) => !arg.startsWith('-'));
  const jsonPath = inputArg ? path.resolve(process.cwd(), inputArg) : DEFAULT_JSON_PATH;

  if (!fs.existsSync(jsonPath)) {
    throw new Error(`JSON file not found: ${jsonPath}`);
  }

  if (!admin.apps.length) {
    admin.initializeApp({
      credential: admin.credential.applicationDefault()
    });
  }

  const pois = readJsonArray(jsonPath);
  pois.forEach(validatePoi);

  if (dryRun) {
    console.log('Dry run only. No Firestore writes performed.');
    console.log(`Collection: ${POIS_COLLECTION}`);
    console.log(`Input file: ${jsonPath}`);
    console.log(`Total POIs: ${pois.length}`);
    console.log('POI IDs:');
    pois.forEach((poi) => console.log(`- ${poi.id}`));
    return;
  }

  const result = await upsertPois(pois);

  console.log('Firestore upsert complete.');
  console.log(`Collection: ${POIS_COLLECTION}`);
  console.log(`Input file: ${jsonPath}`);
  console.log(`Total: ${result.total}, Created: ${result.created}, Updated: ${result.updated}`);
}

main().catch((error) => {
  console.error('Import failed:', error.message || error);
  process.exit(1);
});

