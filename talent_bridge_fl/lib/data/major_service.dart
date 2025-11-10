import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:talent_bridge_fl/domain/major_entity.dart';

class MajorService {
  static Future<List<MajorEntity>> getMajors() async {
    final store = FirebaseFirestore.instance;
    final majors = (await store.collection('majors').orderBy('name').get()).docs
        .map((e) => MajorEntity.fromMap(e.data()))
        .toList();
    return majors;
  }

  static List<String> getFallbackMajors() {
    return [
      'Administración',
      'Arquitectura',
      'Diseño',
      'Arte',
      'Historia del Arte',
      'Literatura',
      'Música',
      'Narrativas Digitales',
      'Biología',
      'Física',
      'Geociencias',
      'Matemáticas',
      'Microbiología',
      'Química',
      'Antropología',
      'Ciencia Política',
      'Estudios Globales',
      'Filosofía',
      'Historia',
      'Lenguas y Cultura',
      'Psicología',
      'Derecho',
      'Economía',
      'Licenciatura en Artes',
      'Licenciatura en Biología',
      'Licenciatura en Edu. Infantil',
      'Licenciatura en Español y Filología',
      'Licenciatura en Filosofía',
      'Licenciatura en Física',
      'Licenciatura en Historia',
      'Licenciatura en Matemáticas',
      'Licenciatura en Química',
      'Ingeniería Ambiental',
      'Ingeniería Biomédica',
      'Ingeniería Civil',
      'Ingeniería Eléctrica',
      'Ingeniería Electrónica',
      'Ingeniería Industrial',
      'Ingeniería Mecánica',
      'Ingeniería Química',
      'Ingeniería de Alimentos',
      'Ingeniería de Sistemas y Computación',
      'Medicina',
      'Gobierno y Asuntos Públicos',
      'Estudios Dirigidos',
    ];
  }
}
