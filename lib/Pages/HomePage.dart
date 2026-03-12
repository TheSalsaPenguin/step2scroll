import 'package:flutter/material.dart';
import 'package:latlong2/latlong.dart';

class Homepage extends StatelessWidget {
  final String steps;
  late double time = int.parse(steps)/1000;
  late double minutes = round(time*60);

  Homepage({super.key, required this.steps});

  @override
  Widget build(BuildContext context) {
    return Center( // Optional: centers the column
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Text(
            "Du har i dag gået $steps skridt.",
            style: const TextStyle(fontSize: 24),
          ),
          Text(
            "Det giver dig ${(minutes/60).toStringAsFixed(0)} timer og ${(minutes%60).toStringAsFixed(0)} minutters skærmtid.",
            style: const TextStyle(fontSize: 24),
            textAlign: TextAlign.center,
          ),

        ],
      ),
    );
  }
}

