wentingt

rate (unit: B / second)

RateLimiter.create(rate / k)			NA * rate / CHUNKSIZE		
	(unit: permits / second)

RateLimiter.acquire(CHUNKSIZE / k) -- 1		NA
	1 / k (unit: permits / B)				

total number of acquire = fileLength / CHUNKSIZE -- need to be small

Thus we need to make CHUNKSIZE big 
- it cannot be too big because too coarse grained)
- it cannot be too big because channel separate it.


FileRegion: near 1GB

ChunkedFile: Degrade.

Tested on max_clemson
iperf: 2Gb 
FileRegion pass.
