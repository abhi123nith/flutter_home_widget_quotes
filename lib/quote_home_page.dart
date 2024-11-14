import 'dart:io';
import 'package:flutter/material.dart';
import 'package:home_widget/home_widget.dart';
import 'package:home_widget_counter/provider/quotes_provider.dart';
import 'package:provider/provider.dart';

@pragma('vm:entry-point')
Future<void> interactiveCallback(Uri? uri) async {
  await HomeWidget.setAppGroupId('group.es.antonborri.homeWidgetCounter');
  if (uri?.host == 'fetchQuote') {
    await _fetchAndDisplayQuote();
  }
}

const _quoteKey = 'quote';

Future<void> _fetchAndDisplayQuote() async {
  final provider = QuoteProvider();
  await provider.fetchQuote();
  await HomeWidget.saveWidgetData(_quoteKey, provider.currentQuote);
  await HomeWidget.updateWidget(
    iOSName: 'QuoteWidget',
    androidName: 'QuoteWidgetProvider',
  );
  if (Platform.isAndroid) {
    await HomeWidget.updateWidget(androidName: 'QuoteGlanceWidgetReceiver');
  }
}

class QuoteHomePage extends StatefulWidget {
  const QuoteHomePage({super.key, required this.title});
  final String title;

  @override
  State<QuoteHomePage> createState() => _QuoteHomePageState();
}

class _QuoteHomePageState extends State<QuoteHomePage> with WidgetsBindingObserver {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);

    // Trigger data fetch only after the first frame is rendered
    WidgetsBinding.instance.addPostFrameCallback((_) {
      Provider.of<QuoteProvider>(context, listen: false).fetchQuote();
    });
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    super.didChangeAppLifecycleState(state);
    if (state == AppLifecycleState.resumed) {
      Provider.of<QuoteProvider>(context, listen: false).fetchQuote();
    }
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    super.dispose();
  }

  Future<void> _fetchNewQuote() async {
    await Provider.of<QuoteProvider>(context, listen: false).fetchQuote();
  }

  Future<void> _requestToPinWidget() async {
    final isRequestPinSupported = await HomeWidget.isRequestPinWidgetSupported();
    // print(isRequestPinSupported);
    if (isRequestPinSupported == true) {
      await HomeWidget.requestPinWidget(
        androidName: 'QuoteGlanceWidgetReceiver',
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final quoteProvider = Provider.of<QuoteProvider>(context);

    return Scaffold(
      appBar: AppBar(title: Text(widget.title)),
      body: Center(
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 15.0),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: <Widget>[
              const Text(
                  'Current Quote:',
                style: TextStyle(
                  fontSize: 18,
                  fontWeight: FontWeight.bold
                ),
              ),
              const SizedBox(height: 20,),
              if (quoteProvider.isFetching)
                const CircularProgressIndicator()
              else
                Text(
                  quoteProvider.currentQuote,
                  textAlign: TextAlign.center,
                  style: TextStyle(
                    fontSize: 20,
                    fontStyle: FontStyle.italic
                  ),
                ),
              const SizedBox(height: 30,),
              ElevatedButton(
                onPressed: _fetchNewQuote,
                child: const Text('Fetch New Quote'),
              ),
              GestureDetector(
                onTap: _requestToPinWidget,
                child: const Text('Pin Widget to Home Screen'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
