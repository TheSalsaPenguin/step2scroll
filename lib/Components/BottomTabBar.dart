import 'package:flutter/material.dart';
import 'package:step2scroll/Pages/HomePage.dart';
import 'package:step2scroll/Pages/Map.dart';

class CustomBottomNavBar extends StatefulWidget {
  final String steps;

  const CustomBottomNavBar({super.key, required this.steps});

  @override
  State<CustomBottomNavBar> createState() => _CustomBottomNavBarState();
}

class _CustomBottomNavBarState extends State<CustomBottomNavBar> {
  int _selectedIndex = 0;

  static const TextStyle optionStyle =
  TextStyle(fontSize: 30, fontWeight: FontWeight.bold);

  @override
  Widget build(BuildContext context) {
    final List<Widget> widgetOptions = <Widget>[
      Homepage(steps: widget.steps),
     OSMFlutterMap(),
      const Text('Skærmtid', style: optionStyle),
    ];

    return Scaffold(
      body: Center(child: widgetOptions[_selectedIndex]),
      bottomNavigationBar: BottomNavigationBar(
        items: const [
          BottomNavigationBarItem(icon: Icon(Icons.home), label: 'Hjem'),
          BottomNavigationBarItem(icon: Icon(Icons.map), label: 'Kort'),
          BottomNavigationBarItem(icon: Icon(Icons.hourglass_bottom), label: 'Skærmtid' )
        ],
        currentIndex: _selectedIndex,
        selectedItemColor: Colors.blue,
        onTap: (index) => setState(() => _selectedIndex = index),
      ),
    );
  }
}
