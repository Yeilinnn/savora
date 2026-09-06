db = db.getSiblingDB('savora');

// 2b: validador — un perfil sin estos campos, o con un tipo/valor fuera de rango,
// la base lo rechaza directamente, no solo la validación de Java.
db.createCollection('perfil_impacto', {
  validator: {
    $jsonSchema: {
      bsonType: 'object',
      required: [
        'propietarioId', 'tipoPropietario', 'kgRescatados',
        'reservasRecogidas', 'reservasNoRecogidas',
        'paquetesDonados', 'paquetesPerdidos', 'insignias'
      ],
      properties: {
        propietarioId: { bsonType: 'string' },
        tipoPropietario: { enum: ['CLIENTE', 'NEGOCIO'] },
        kgRescatados: { bsonType: ['double', 'decimal', 'int', 'long'], minimum: 0 },
        reservasRecogidas: { bsonType: 'int', minimum: 0 },
        reservasNoRecogidas: { bsonType: 'int', minimum: 0 },
        paquetesDonados: { bsonType: 'int', minimum: 0 },
        paquetesPerdidos: { bsonType: 'int', minimum: 0 },
        insignias: {
          bsonType: 'array',
          items: {
            bsonType: 'object',
            required: ['codigo', 'fechaObtenida'],
            properties: {
              codigo: {
                enum: ['PRIMER_RESCATE', 'GUARDIAN_DEL_BARRIO', 'RACHA_3_SEMANAS', 'CERO_DESPERDICIO']
              },
              fechaObtenida: { bsonType: 'date' }
            }
          }
        }
      }
    }
  }
});

// 2a reforzado aquí también: mismo índice único que @CompoundIndex crea desde
// Java, para que exista aunque la app nunca arranque.
db.perfil_impacto.createIndex(
  { propietarioId: 1, tipoPropietario: 1 },
  { unique: true, name: 'uq_propietario_tipo' }
);

// 2c: los mismos 5 perfiles que hoy siembra MongoSeeder.java, disponibles con
// solo `docker compose up`, sin depender de que la aplicación arranque.
db.perfil_impacto.insertMany([
  {
    propietarioId: '1', tipoPropietario: 'CLIENTE', kgRescatados: 6.5,
    reservasRecogidas: 1, reservasNoRecogidas: 0, paquetesDonados: 0, paquetesPerdidos: 0,
    insignias: [{ codigo: 'PRIMER_RESCATE', fechaObtenida: new Date(Date.now() - 6 * 24 * 60 * 60 * 1000) }]
  },
  {
    propietarioId: '2', tipoPropietario: 'CLIENTE', kgRescatados: 3.0,
    reservasRecogidas: 1, reservasNoRecogidas: 0, paquetesDonados: 0, paquetesPerdidos: 0,
    insignias: [{ codigo: 'PRIMER_RESCATE', fechaObtenida: new Date(Date.now() - 3 * 24 * 60 * 60 * 1000) }]
  },
  {
    propietarioId: '3', tipoPropietario: 'CLIENTE', kgRescatados: 0,
    reservasRecogidas: 0, reservasNoRecogidas: 1, paquetesDonados: 0, paquetesPerdidos: 0,
    insignias: []
  },
  {
    propietarioId: '1', tipoPropietario: 'NEGOCIO', kgRescatados: 4.0,
    reservasRecogidas: 0, reservasNoRecogidas: 0, paquetesDonados: 1, paquetesPerdidos: 0,
    insignias: []
  },
  {
    propietarioId: '2', tipoPropietario: 'NEGOCIO', kgRescatados: 0,
    reservasRecogidas: 0, reservasNoRecogidas: 0, paquetesDonados: 0, paquetesPerdidos: 1,
    insignias: []
  }
]);