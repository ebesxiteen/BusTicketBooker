SELECT tripId, busId 
FROM Trips 
WHERE busId NOT IN (SELECT busId FROM Buses);
