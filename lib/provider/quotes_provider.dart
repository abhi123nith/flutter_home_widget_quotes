import 'dart:convert';
import 'dart:math';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;

class QuoteProvider with ChangeNotifier {
  static const String _apiUrl = 'https://staticapis.pragament.com/daily/quotes-en-gratitude.json';
  String _currentQuote = "Fetching...";
  bool _isFetching = false;

  String get currentQuote => _currentQuote;
  bool get isFetching => _isFetching;

  // Fetch quotes from the API and update the current quote
  Future<void> fetchQuote() async {
    _isFetching = true;
    notifyListeners();

    try {
      final response = await http.get(Uri.parse(_apiUrl));
      if (response.statusCode == 200) {
        final data = json.decode(response.body);
        final quotes = data['quotes'] as List;
        if (quotes.isNotEmpty) {
          _currentQuote = quotes[Random().nextInt(quotes.length)]['quote'];
        }
      } else {
        _currentQuote = "Failed to fetch quote.";
      }
    } catch (e) {
      _currentQuote = "Error: ${e.toString()}";
    } finally {
      _isFetching = false;
      notifyListeners();
    }
  }
}
