import 'package:flutter/material.dart';
import 'db_helper.dart';

void main() => runApp(MyApp());

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Dictionary App',
      home: DictionaryScreen(),
    );
  }
}

class DictionaryScreen extends StatefulWidget {
  @override
  _DictionaryScreenState createState() => _DictionaryScreenState();
}

class _DictionaryScreenState extends State<DictionaryScreen> {
  final TextEditingController _controller = TextEditingController();
  final DBHelper dbHelper = DBHelper();
  List<Map<String, dynamic>> results = [];

  void _lookup() async {
    String query = _controller.text.trim().toLowerCase();
    if (query.isEmpty) return;

    final res = await dbHelper.searchWord(query);
    setState(() {
      results = res;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text('Dictionary')),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          children: [
            TextField(
              controller: _controller,
              decoration: InputDecoration(
                labelText: 'Enter a word',
                suffixIcon: IconButton(
                  icon: Icon(Icons.search),
                  onPressed: _lookup,
                ),
              ),
            ),
            SizedBox(height: 20),
            Expanded(
              child: results.isEmpty
                  ? Text('No results found.')
                  : ListView.builder(
                itemCount: results.length,
                itemBuilder: (context, index) {
                  final item = results[index];
                  return ListTile(
                    title: Text(item['word']),
                    subtitle: Text(item['definition']),
                  );
                },
              ),
            ),
          ],
        ),
      ),
    );
  }
}
