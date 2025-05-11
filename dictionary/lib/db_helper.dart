import 'package:sqflite/sqflite.dart';
import 'package:path/path.dart';
import 'package:path_provider/path_provider.dart';
import 'dart:io';

class DBHelper {
  static Database? _db;

  Future<Database> get db async {
    if (_db != null) return _db!;
    _db = await initDb();
    return _db!;
  }

  initDb() async {
    Directory documentsDirectory = await getApplicationDocumentsDirectory();
    String path = join(documentsDirectory.path, "dictionary.db");
    return await openDatabase(path, version: 1, onCreate: _onCreate);
  }

  void _onCreate(Database db, int version) async {
    await db.execute('''
      CREATE TABLE dictionary (
        id INTEGER PRIMARY KEY,
        word TEXT,
        definition TEXT
      )
    ''');
    await db.insert("dictionary", {'word': 'apple', 'definition': 'A red fruit'});
    await db.insert("dictionary", {'word': 'application', 'definition': 'A formal request or a software'});
    await db.insert('dictionary', {'word': 'verisimilitude', 'definition': 'the appearance of being true or real'});
    await db.insert('dictionary', {'word': 'realism', 'definition': 'representation of things as they actually are'});
    await db.insert('dictionary', {'word': 'serendipity', 'definition': 'finding valuable things not sought for'});
    await db.insert('dictionary', {'word': 'ephemeral', 'definition': 'lasting for a very short time'});
    await db.insert('dictionary', {'word': 'lucid', 'definition': 'expressed clearly; easy to understand'});
    await db.insert('dictionary', {'word': 'ineffable', 'definition': 'too great to be expressed in words'});
    await db.insert('dictionary', {'word': 'sonder', 'definition': 'the realization that everyone has a complex life'});
    await db.insert('dictionary', {'word': 'limerence', 'definition': 'the state of being infatuated'});
    await db.insert('dictionary', {'word': 'sonder', 'definition': 'the realization that everyone has a life as vivid as your own'});
    await db.insert('dictionary', {'word': 'epoch', 'definition': 'a particular period of time in history'});
    await db.insert('dictionary', {'word': 'sonder', 'definition': 'realizing each passerby has a life as rich as yours'});
    await db.insert('dictionary', {'word': 'solitude', 'definition': 'the state of being alone'});

  }

  Future<List<Map<String, dynamic>>> searchWord(String input) async {
    final dbClient = await db;
    final exact = await dbClient.query(
      'dictionary',
      where: 'word = ?',
      whereArgs: [input],
    );
    if (exact.isNotEmpty) return exact;

    return await dbClient.query(
      'dictionary',
      where: 'word LIKE ?',
      whereArgs: ['%$input%'],
    );
  }
}
