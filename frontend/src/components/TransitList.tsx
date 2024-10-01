import { useEffect, useState, useRef } from 'react';
import { Arrivals, Station, ChildStop, LineArrival } from '../App';

interface TransitListProps {
    arrivals: Arrivals;
    className: string;
    selectedStationId: string | null;
}

const TransitList = ({ arrivals, className, selectedStationId }: TransitListProps) => {
    // apparently this is a workaround to force a re-render when the arrivals prop changes
    const [, setUpdateTrigger] = useState(0);
    const stationRefs = useRef<{ [key: string]: HTMLDivElement | null }>({});
    useEffect(() => {
        setUpdateTrigger(prev => prev + 1);
    }, [arrivals]);

    // when a station on the map is selected, this will scroll to it in the station list
    useEffect(() => {
        if (selectedStationId && stationRefs.current[selectedStationId]) {
            stationRefs.current[selectedStationId]?.scrollIntoView({ behavior: 'smooth', block: 'center' });
        }
    }, [selectedStationId]);

    return (
        <div className={className}>
            <h1 className="subwayHeaders">Subway Next Arrivals</h1>
            <ul>
                {Object.entries(arrivals.stops as Record<string, Station>).map(([stationId, station]) => (
                    <div
                        key={stationId}
                        ref={(el) => (stationRefs.current[stationId] = el)}
                        className={`card ${stationId === selectedStationId ? 'selected' : ''}`}
                    >
                        <StationComponent
                            station={station}
                            isSelected={stationId === selectedStationId}
                        />
                    </div>
                ))}
            </ul>
        </div>
    );
};

const StationComponent = ({ station }: { station: Station, isSelected: boolean }) => {
    return (
        <li className="station">
            <h2 className="subwayHeaders">{station.name}</h2>
            <ul>
                {station.childStops && station.childStops.length > 0 ? (
                    station.childStops.map((platform) => (
                        <PlatformComponent key={platform.id} platform={platform} />
                    ))
                ) : (
                    <li>No platforms available</li>
                )}
            </ul>
        </li>
    );
};

const PlatformComponent = ({ platform }: { platform: ChildStop }) => {
    return (
        <li className="platform">
            <ul>
                {Object.entries(platform.lineArrivals).length > 0 ? (
                    Object.entries(platform.lineArrivals).map(([lineId, arrival]) => (
                        <LineArrivalComponent
                            key={`${platform.id}-${lineId}-${arrival.currentArrivalTime}`}
                            lineId={lineId}
                            arrival={arrival}
                            platformId={platform.id}
                        />
                    ))
                ) : (
                    <li>
                        <i>No {platform.id.endsWith('N') ? 'northbound' : 'southbound'} arrivals</i>
                    </li>
                )}
            </ul>
        </li>
    );
};

const LineArrivalComponent = ({ arrival, lineId, platformId }: { arrival: LineArrival; lineId: string; platformId: string }) => {
    const textColor = arrival.color === 'FCCC0A' ? '#202010' : '#FEFEF0';
    const backgroundColor = arrival.color || '7C878E';

    return (
        <li style={{ display: 'flex', alignItems: 'center' }}>
            <div style={{ flex: '0 0 116px', whiteSpace: 'nowrap' }}>
                {new Date(arrival.currentArrivalTime * 1000).toLocaleTimeString()}
            </div>
            <div style={{ flex: '0 0 50px', whiteSpace: 'nowrap' }}>
                <span className="subway-sign" style={{ backgroundColor: `#${backgroundColor}`, color: textColor }}>
                    {lineId}
                </span>
            </div>
            <div style={{ flex: '0 0 30px', whiteSpace: 'nowrap' }}>
                {platformId.endsWith('N') ? '↑' : '↓'}
            </div>
            <div style={{ flex: '1' }}>
                {arrival.destination}
            </div>
        </li>
    );
};

export default TransitList;
