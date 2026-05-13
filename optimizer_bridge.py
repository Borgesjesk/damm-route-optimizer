import sys
import json
import math

def haversine(lat1, lng1, lat2, lng2):
    R = 6371
    dlat = math.radians(lat2 - lat1)
    dlng = math.radians(lng2 - lng1)
    a = math.sin(dlat/2)**2 + math.cos(math.radians(lat1)) * math.cos(math.radians(lat2)) * math.sin(dlng/2)**2
    return R * 2 * math.atan2(math.sqrt(a), math.sqrt(1-a))

def two_opt(clients):
    route = sorted(clients, key=lambda c: haversine(clients[0]['lat'], clients[0]['lng'], c['lat'], c['lng']))
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

def cluster_by_walking_distance(route, max_walk_km=0.3):
    """Group consecutive stops within 300m walking distance"""
    clusters = []
    current_cluster = [route[0]]
    for i in range(1, len(route)):
        dist = haversine(current_cluster[0]['lat'], current_cluster[0]['lng'], route[i]['lat'], route[i]['lng'])
        if dist <= max_walk_km:
            current_cluster.append(route[i])
        else:
            clusters.append(current_cluster)
            current_cluster = [route[i]]
    clusters.append(current_cluster)
    return clusters

data = json.loads(sys.stdin.read())
optimized = two_opt(data)
clusters = cluster_by_walking_distance(optimized)

# Flatten clusters back to ordered list — keeps nearby clients together
result = []
for cluster in clusters:
    result.extend(cluster)

print(json.dumps([c['id'] for c in result]))
