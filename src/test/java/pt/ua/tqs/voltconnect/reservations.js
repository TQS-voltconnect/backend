import http from "k6/http";
import { check, sleep, group } from "k6";
import { Trend, Rate } from "k6/metrics";

// Define custom metrics
let responseTimeTrend = new Trend("response_time", true); // Track response time
let successRate = new Rate("success_rate"); // Track success rate

export let options = {
  // Define the load test behavior
  stages: [
    { duration: "5m", target: 50 }, // Ramp-up to 50 VUs over 5 minutes
    { duration: "5m", target: 100 }, // Ramp-up to 100 VUs over 5 minutes
    { duration: "5m", target: 150 }, // Ramp-up to 150 VUs over 5 minutes
    { duration: "5m", target: 100 }, // Hold 100 VUs for 5 minutes
    { duration: "5m", target: 150 }, // Spike to 150 VUs for 5 minutes
    { duration: "5m", target: 0 }, // Ramp-down to 0 VUs over 5 minutes
  ],
  thresholds: {
    // Define thresholds for different metrics
    http_req_duration: ["p(95)<100"], // 95% of requests should finish under 100ms
    http_req_failed: ["rate<0.01"], // Less than 1% failure rate
    success_rate: ["rate==1"], // Success rate should be 100%
    response_time: ["avg<20"], // Average response time should be under 20ms
  },
};

const BASE_URL = "http://localhost:8080/api/reservations"; // API endpoint for creating reservations
const stationId = 1;
const chargerId = 1;
const vehicleIdBase = "a7805129-057a-45a1-b9a5-3cef58aceb3d"; // Placeholder vehicle ID
const userIdBase = 1; // Placeholder user ID

export default function () {
  group("Reservation Creation", () => {
    const vu = __VU;
    const iter = __ITER;
    const now = new Date();
    const randomOffset = Math.floor(Math.random() * 10 * 60 * 1000); // Add a random offset for variation
    const chargingTime = 60; // Charging time in minutes

    // Calculate the start time to prevent overlapping reservations
    const startTime = new Date(
      now.getTime() +
        vu * 24 * 60 * 60 * 1000 + // Ensures that each virtual user has a different "date"
        iter * chargingTime * 60 * 1000 + // Adds the incremental offset
        randomOffset // Random offset for variation
    );

    const payload = JSON.stringify({
      userId: userIdBase,
      vehicleId: vehicleIdBase,
      chargingStationId: stationId,
      chargerId: chargerId,
      startTime: startTime.toISOString(),
      chargingTime: chargingTime,
    });

    const params = {
      headers: {
        "Content-Type": "application/json",
      },
    };

    let res = http.post(BASE_URL, payload, params);

    // Track response time and success rate
    responseTimeTrend.add(res.timings.duration);
    successRate.add(res.status === 200 || res.status === 201);

    // Check if the status is 200 or 201 and no conflict
    check(res, {
      "status is 200 or 201": (r) => r.status === 200 || r.status === 201,
      "no reservation conflict": (r) =>
        !r.body.includes("conflicts with an existing reservation"),
    });

    sleep(1); // Sleep between iterations
  });
}
