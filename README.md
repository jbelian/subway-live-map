# Subway Live Map

This app fetches updates from the [MTA](https://new.mta.info/) data feed via an open source REST API, [Transiter](https://docs.transiter.dev/), continuously finding the earliest arrivals for every subway stop in New York City. It also tracks the exponential moving average of wait times for every arrival, smoothing out spikes in the data while still giving a visual representation of average wait times, bottlenecks, and delays. Each station on the map is colored by the average of all of that station's arrival wait times, with times closer to 0 minutes being bluer, and those closer to 20+ as redder. 

For tracking arrival times, it seems generally accurate and in-line with other apps like https://realtimerail.nyc/, which uses the same API on the backend.
