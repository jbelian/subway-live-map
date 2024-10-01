import { QueryClient, QueryClientProvider, useQuery } from '@tanstack/react-query';
import { useState } from 'react';
import TransitList from './components/TransitList';
import TransitMap from './components/TransitMap';

export type Arrivals = {
  stops: {
    [stationId: string]: Station;
  };
}

// station
export type Station = {
  id: string;
  name: string;
  latitude: number;
  longitude: number;
  type: string;
  childStops: ChildStop[];
  stopTimes: null;
}

// platform
export type ChildStop = {
  id: string;
  name: string;
  lineArrivals: {
    [lineId: string]: LineArrival;
  };
}

// line
export type LineArrival = {
  tripId: string;
  lineId: string;
  color: string;
  destination: string;
  currentArrivalTime: number;
  previousArrivalTime: number;
  movingAverage: number;
}

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      retry: false,
    },
  },
});

const fetchArrivals = async (): Promise<Arrivals> => {
  const response = await fetch(`/api/arrivals`);
  if (!response.ok) {
    throw new Error(`Network response was not ok: ${response.status} ${response.statusText}`);
  }

  const data = await response.json();
  return data;
};

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <Content />
    </QueryClientProvider>
  );
}

function Content() {
  const { data, isLoading, error, refetch } = useQuery<Arrivals, Error>({
    queryKey: ['Arrivals'],
    queryFn: fetchArrivals,
    refetchInterval: 15000, // 15 seconds
  });

  const [selectedStationId, setSelectedStationId] = useState<string | null>(null);

  if (isLoading) return <div>Loading...</div>;
  if (error) return (
    <div>
      <p>Error: {error.message}</p>
      <button onClick={() => refetch()}>Retry</button>
    </div>
  );
  if (!data) return <div>No data available</div>;

  const arrivals = { ...data };

  const handleStationSelect = (stationId: string) => {
    setSelectedStationId(stationId);
  };

  return (
    <div className="transit-container">
      <TransitMap 
        className="transit-map" 
        arrivals={arrivals} 
        onStationSelect={handleStationSelect}
      />
      <TransitList 
        className="transit-list" 
        arrivals={arrivals} 
        selectedStationId={selectedStationId}
      />
    </div>
  );
}

export default App;