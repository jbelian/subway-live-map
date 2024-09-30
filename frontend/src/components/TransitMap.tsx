import React, { useEffect, useRef } from 'react';
import L from 'leaflet';
import { MapContainer, TileLayer, useMap } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import { Arrivals, Station, LineArrival } from '../App';

interface TransitMapProps {
  arrivals: Arrivals;
  className: string;
  onStationSelect: (stationId: string) => void;
}

// colors the station marker based on the average wait time: bluer for short waits, redder for long waits
const getColor = (wait: number): string => {
  if (wait == 0 || isNaN(wait)) return '#000000';
  if (wait <= 4) return '#1065AB';
  if (wait <= 6) return '#3A93C3';
  if (wait <= 8) return '#8EC4DE';
  if (wait <= 10) return '#D1E5F0';
  if (wait <= 12) return '#F9F9F9';
  if (wait <= 14) return '#FEDBC7';
  if (wait <= 16) return '#F6A482';
  if (wait <= 18) return '#D75F4C';
  return '#B31529';
};

const formatLineWaits = (lineArrivals: { [lineId: string]: LineArrival }) => {
  return Object.entries(lineArrivals).map(([lineId, arrival]) => {
    const textColor = arrival.color === 'FCCC0A' ? '#202010' : '#FEFEF0';
    const backgroundColor = arrival.color || '7C878E'; // Fallback

    return `
      <div style="display: flex;">
        <span class="subway-sign-popup" style="--sign-background: #${backgroundColor}; --sign-text: ${textColor};">
          ${lineId}
        </span>
        <span class="popup-platform-text">${arrival.movingAverage.toFixed(2)} minutes</span>
      </div>`;
  }).join(', ');
};

function MapContent({ arrivals, onStationSelect }: TransitMapProps) {
  const map = useMap();
  const markersRef = useRef<{ [key: string]: L.CircleMarker }>({});

  useEffect(() => {
    const updateMarkers = () => {
      Object.values(arrivals.stops).forEach((station: Station) => {
        const northboundArrivals = station.childStops.find(platform => platform.id.endsWith('N'))?.lineArrivals || {};
        const southboundArrivals = station.childStops.find(platform => platform.id.endsWith('S'))?.lineArrivals || {};

        const northboundWaits = formatLineWaits(northboundArrivals);
        const southboundWaits = formatLineWaits(southboundArrivals);

        // While every line arrival has its own average wait time, the station marker colors can 
        // really only represent the average values of all wait times at that station.
        const allWaits = [...Object.values(northboundArrivals), ...Object.values(southboundArrivals)]
          .map(arrival => arrival.movingAverage);
        const averageWait = allWaits.length > 0 ? allWaits.reduce((a, b) => a + b, 0) / allWaits.length : null;

        // the popup itself lists all the wait times for each line
        const northboundListItems = northboundWaits.length > 0
          ? northboundWaits.split(', ').map(wait =>
            `<div>${wait}</div>`).join('')
          : '<div><i>No arrivals</i></div>';

        const southboundListItems = southboundWaits.length > 0
          ? southboundWaits.split(', ').map(wait =>
            `<div>${wait}</div>`).join('')
          : '<div><i>No arrivals</i></div>';

        const popupContent = `
          <div class="mapPopup">
            <h3>${station.name}</h3>
            <p>Average arrival wait times:</p>
            <ul>Northbound
              ${northboundListItems}
            </ul>
            <ul>Southbound
              ${southboundListItems}
            </ul>
          </div>
        `;

        const popupOptions = {
          closeButton: false,
          offset: L.point(0, 0),
          maxWidth: 450,
        };

        if (markersRef.current[station.id]) {
          const marker = markersRef.current[station.id];
          marker.setStyle({
            fillColor: getColor(averageWait || 0),
          });
          marker.unbindPopup().bindPopup(popupContent, popupOptions);
        } else {
          const marker = L.circleMarker([station.latitude, station.longitude], {
            radius: 10,
            fillColor: getColor(averageWait || 0),
            color: 'black',
            weight: 1,
            opacity: 1,
            fillOpacity: 0.8,
          }).addTo(map);

          marker.bindPopup(popupContent, popupOptions);

          marker.on('click', function (this: L.CircleMarker) {
            this.openPopup();
            onStationSelect(station.id);
          });

          markersRef.current[station.id] = marker;
        }
      });

      Object.keys(markersRef.current).forEach((stationId) => {
        if (!arrivals.stops[stationId]) {
          map.removeLayer(markersRef.current[stationId]);
          delete markersRef.current[stationId];
        }
      });
    };

    updateMarkers();
  }, [arrivals, map, onStationSelect]);

  return null;
}

function TransitMap({ arrivals, className, onStationSelect }: TransitMapProps) {
  return (
    <div className={className}>
      <MapContainer
        center={[40.7128, -74.0060]}
        zoom={12}
        style={{ height: '100%', width: '100%' }}
        key="map-container"
      >
        <TileLayer
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
        />
        <MapContent arrivals={arrivals} onStationSelect={onStationSelect} className={className} />
      </MapContainer>
    </div>
  );
}

export default React.memo(TransitMap);