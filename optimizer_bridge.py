import sys
import json
import math

def haversine(lat1, lng1, lat2, lng2):
    R = 6371
    dlat = math.radians(lat2 - lat1)
    dlng = math.radians(lng2 - lng1)
    a = math.sin(dlat/2)**2 + math.cos(math.radians(lat1)) * math.cos(math.radians(lat2)) * math.sin(dlng/2)**2
    return R * 2 * math.atan2(math.sqrt(a), math.sqrt(1-a))

def nearest_neighbor(clients):
    if not clients:
        return []
    unvisited = list(clients)
    route = [unvisited.pop(0)]
    while unvisited:
        last = route[-1]
        nearest = min(unvisited, key=lambda c: haversine(last['lat'], last['lng'], c['lat'], c['lng']))
        route.append(nearest)
        unvisited.remove(nearest)
    return route

def two_opt(clients):
    route = nearest_neighbor(clients)
    improved = True
    while improved:
        improved = False
        for i in range(1, len(route) - 1):
            for j in range(i + 1, len(route)):
                d_before = haversine(route[i-1]['lat'], route[i-1]['lng'], route[i]['lat'], route[i]['lng']) + haversine(route[j]['lat'], route[j]['lng'], route[(j+1) % len(route)]['lat'], route[(j+1) % len(route)]['lng'])
                d_after = haversine(route[i-1]['lat'], route[i-1]['lng'], route[j]['lat'], route[j]['lng']) + haversine(route[i]['lat'], route[i]['lng'], route[(j+1) % len(route)]['lat'], route[(j+1) % len(route)]['lng'])
                if d_after < d_before:
                    route[i:j+1] = reversed(route[i:j+1])
                    improved = True
    return route

data = json.loads(sys.stdin.read())
result = two_opt(data)
print(json.dumps([c['id'] for c in result]))
