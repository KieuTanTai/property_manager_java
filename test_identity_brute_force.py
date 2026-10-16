#!/usr/bin/env python3
"""
Brute Force Test Script for Identity Module
Tests RabbitMQ and Queue handling under high load

Endpoints:
- POST /api/account/register
- POST /api/account/login

Usage:
    python3 test_identity_brute_force.py --url http://localhost:8081
    python3 test_identity_brute_force.py --url https://localhost:8081
    python3 test_identity_brute_force.py --url http://localhost:8081 --workers 100 --duration 60
"""

import argparse
import asyncio
import aiohttp
import random
import string
import time
import json
from datetime import datetime
from typing import Dict, List, Tuple
from dataclasses import dataclass, field


@dataclass
class TestStats:
    """Statistics tracker for the test"""

    login_requests: int = 0
    login_success: int = 0
    login_failures: int = 0
    register_requests: int = 0
    register_success: int = 0
    register_failures: int = 0
    total_response_time: float = 0.0
    error_count: int = 0
    status_codes: Dict[int, int] = field(default_factory=dict)


@dataclass
class TestConfig:
    """Test configuration"""

    base_url: str
    workers: int
    duration_seconds: int
    use_https: bool
    login_weight: float = 0.7  # 70% login, 30% register


def generate_random_email(prefix: str = "test") -> str:
    """Generate random email address"""
    random_suffix = ''.join(random.choices(string.ascii_lowercase + string.digits, k=8))
    return f"{prefix}{random_suffix}@example.com"


def generate_random_password(length: int = 12) -> str:
    """Generate random password"""
    chars = string.ascii_letters + string.digits + "!@#$%^&*"
    return ''.join(random.choices(chars, k=length))


def generate_test_user() -> Tuple[str, str]:
    """Generate a random test user"""
    email = generate_random_email()
    password = generate_random_password()
    return email, password


async def send_login_request(session: aiohttp.ClientSession, base_url: str, email: str, password: str) -> Tuple[bool, int, float]:
    """Send a login request"""
    url = f"{base_url}/api/async-account/login"
    payload = {"email": email, "password": password}
    
    try:
        start_time = time.time()
        async with session.post(url, json=payload, timeout=30) as response:
            elapsed = time.time() - start_time
            status = response.status
            return status == 200, status, elapsed
    except (aiohttp.ClientError, asyncio.TimeoutError) as e:
        return False, 0, 0.0


async def send_register_request(session: aiohttp.ClientSession, base_url: str, email: str, password: str) -> Tuple[bool, int, float]:
    """Send a register request"""
    url = f"{base_url}/api/async-account/register"
    payload = {"email": email, "password": password}
    
    try:
        start_time = time.time()
        async with session.post(url, json=payload, timeout=30) as response:
            elapsed = time.time() - start_time
            status = response.status
            return status in (200, 201), status, elapsed
    except (aiohttp.ClientError, asyncio.TimeoutError) as e:
        return False, 0, 0.0


async def worker(session: aiohttp.ClientSession, base_url: str, config: TestConfig, stats: TestStats, worker_id: int) -> None:
    """Worker task that sends requests continuously"""
    start_time = time.time()
    
    while time.time() - start_time < config.duration_seconds:
        # Randomly choose between login and register based on weights
        if random.random() < config.login_weight:
            # Login request
            email, password = generate_test_user()
            success, status, elapsed = await send_login_request(session, base_url, email, password)
            
            stats.login_requests += 1
            stats.total_response_time += elapsed
            stats.status_codes[status] = stats.status_codes.get(status, 0) + 1
            
            if success:
                stats.login_success += 1
            else:
                stats.login_failures += 1
        else:
            # Register request
            email, password = generate_test_user()
            success, status, elapsed = await send_register_request(session, base_url, email, password)
            
            stats.register_requests += 1
            stats.total_response_time += elapsed
            stats.status_codes[status] = stats.status_codes.get(status, 0) + 1
            
            if success:
                stats.register_success += 1
            else:
                stats.register_failures += 1
        
        # Small delay to prevent overwhelming the system
        await asyncio.sleep(0.01)


async def run_test(config: TestConfig) -> TestStats:
    """Run the brute force test"""
    stats = TestStats()
    
    # Create HTTP client with connection pool
    connector = aiohttp.TCPConnector(limit=config.workers * 2, limit_per_host=config.workers * 2)
    timeout = aiohttp.ClientTimeout(total=30)
    
    async with aiohttp.ClientSession(connector=connector, timeout=timeout) as session:
        # Create worker tasks
        tasks = [
            asyncio.create_task(worker(session, config.base_url, config, stats, i))
            for i in range(config.workers)
        ]
        
        # Wait for all workers to complete
        await asyncio.gather(*tasks)
    
    return stats


def print_stats(stats: TestStats, config: TestConfig, elapsed_time: float) -> None:
    """Print test statistics"""
    total_requests = stats.login_requests + stats.register_requests
    total_success = stats.login_success + stats.register_success
    total_failures = stats.login_failures + stats.register_failures
    
    print("\n" + "=" * 60)
    print("BRUTE FORCE TEST RESULTS")
    print("=" * 60)
    print(f"Duration: {elapsed_time:.2f} seconds")
    print(f"Workers: {config.workers}")
    print(f"Base URL: {config.base_url}")
    print()
    
    print("REQUEST SUMMARY:")
    print(f"  Total Requests: {total_requests}")
    print(f"  Requests/Second: {total_requests / elapsed_time:.2f}")
    print()
    
    print("LOGIN:")
    print(f"  Requests: {stats.login_requests}")
    print(f"  Success: {stats.login_success}")
    print(f"  Failures: {stats.login_failures}")
    print(f"  Success Rate: {stats.login_success / max(1, stats.login_requests) * 100:.2f}%")
    print()
    
    print("REGISTER:")
    print(f"  Requests: {stats.register_requests}")
    print(f"  Success: {stats.register_success}")
    print(f"  Failures: {stats.register_failures}")
    print(f"  Success Rate: {stats.register_success / max(1, stats.register_requests) * 100:.2f}%")
    print()
    
    print("RESPONSE TIMES:")
    print(f"  Avg Response Time: {stats.total_response_time / max(1, total_requests) * 1000:.2f}ms")
    print()
    
    print("HTTP STATUS CODES:")
    for code, count in sorted(stats.status_codes.items()):
        print(f"  {code}: {count}")
    print()
    
    print("OVERALL:")
    print(f"  Total Success: {total_success}")
    print(f"  Total Failures: {total_failures}")
    print(f"  Overall Success Rate: {total_success / max(1, total_requests) * 100:.2f}%")
    print("=" * 60)


def main():
    parser = argparse.ArgumentParser(
        description="Brute Force Test for Identity Module - Tests RabbitMQ and Queue under load"
    )
    parser.add_argument(
        "--url",
        default="http://localhost:8081",
        help="Base URL of Identity module (default: http://localhost:8081)"
    )
    parser.add_argument(
        "--workers",
        type=int,
        default=50,
        help="Number of concurrent workers (default: 50)"
    )
    parser.add_argument(
        "--duration",
        type=int,
        default=30,
        help="Test duration in seconds (default: 30)"
    )
    parser.add_argument(
        "--login-weight",
        type=float,
        default=0.7,
        help="Percentage of requests that are login (0.0-1.0, default: 0.7)"
    )
    
    args = parser.parse_args()
    
    # Validate arguments
    if args.workers <= 0:
        print("Error: Workers must be > 0")
        return
    
    if args.duration <= 0:
        print("Error: Duration must be > 0")
        return
    
    if not (0.0 <= args.login_weight <= 1.0):
        print("Error: Login weight must be between 0.0 and 1.0")
        return
    
    # Normalize URL (ensure it ends with / or not)
    base_url = args.url.rstrip('/')
    
    config = TestConfig(
        base_url=base_url,
        workers=args.workers,
        duration_seconds=args.duration,
        use_https="https" in base_url,
        login_weight=args.login_weight
    )
    
    print(f"Starting brute force test at {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print(f"Target: {base_url}")
    print(f"Workers: {args.workers}")
    print(f"Duration: {args.duration} seconds")
    print(f"Login/Register ratio: {args.login_weight:.0%} login, {(1-args.login_weight):.0%} register")
    print(f"Press Ctrl+C to stop early...\n")
    
    try:
        start_time = time.time()
        stats = asyncio.run(run_test(config))
        elapsed_time = time.time() - start_time
        
        print_stats(stats, config, elapsed_time)
    except KeyboardInterrupt:
        print("\nTest interrupted by user")
    except Exception as e:
        print(f"\nError: {e}")
        import traceback
        traceback.print_exc()


if __name__ == "__main__":
    main()
