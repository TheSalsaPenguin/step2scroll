import 'package:flutter/material.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:latlong2/latlong.dart';
import 'package:url_launcher/url_launcher.dart';
import 'package:location/location.dart';

class OSMFlutterMap extends StatefulWidget {
  const OSMFlutterMap({super.key});

  @override
  State<OSMFlutterMap> createState() => _OSMFlutterMapState();
}

class _OSMFlutterMapState extends State<OSMFlutterMap> {
  LatLng? _location;
  bool _loading = true;
  String? _error;

  final Location _locationService = Location();

  @override
  void initState() {
    super.initState();
    _getDeviceLocation();
  }

  Future<void> _getDeviceLocation() async {
    try {
      bool serviceEnabled = await _locationService.serviceEnabled();
      if (!serviceEnabled) {
        serviceEnabled = await _locationService.requestService();
        if (!serviceEnabled) {
          setState(() {
            _error = 'Location service not enabled';
            _loading = false;
          });
          return;
        }
      }

      PermissionStatus permission = await _locationService.hasPermission();
      if (permission == PermissionStatus.denied) {
        permission = await _locationService.requestPermission();
        if (permission != PermissionStatus.granted) {
          setState(() {
            _error = 'Location permission denied';
            _loading = false;
          });
          return;
        }
      }

      final locData = await _locationService.getLocation();
      if (locData.latitude != null && locData.longitude != null) {
        setState(() {
          _location = LatLng(locData.latitude!, locData.longitude!);
          _loading = false;
        });
      } else {
        setState(() {
          _error = 'Failed to retrieve coordinates';
          _loading = false;
        });
      }
    } catch (e) {
      setState(() {
        _error = e.toString();
        _loading = false;
      });
    }
  }
  @override
  Widget build(BuildContext context) {
    return FlutterMap(
      options: MapOptions(
        initialCenter: _location ?? LatLng (55.496068119365354, 9.472993526816332),
        minZoom: 1
      ),
      children: [
        TileLayer(
          urlTemplate: 'https://tile.thunderforest.com/atlas/{z}/{x}/{y}.png?apikey=bd81b8acfc70463a9c8c9b311f3075a4',
          userAgentPackageName: 'com.example.step2scroll',
        ),
      ],
    );
  }
}