import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        { duration: '30s', target: 50 },   // 점진적 증가 (Ramp Up)
        { duration: '30s', target: 100 },
        { duration: '30s', target: 200 },
        { duration: '30s', target: 300 },
        { duration: '30s', target: 500 },  // 포화지점 탐색
        { duration: '1m',  target: 500 },  // 유지
        { duration: '30s', target: 0 },    // Ramp Down
    ],
};

export default function () {
    const res = http.get('http://localhost:8080/api/search/v2?keyword=%EC%99%80%EC%9D%B8&page=0&size=10');
    check(res, {
        'status is 200': (r) => r.status === 200,
        'response time < 500ms': (r) => r.timings.duration < 500,
    });
    sleep(0.1);
}