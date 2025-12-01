----锁的key
--local key = KEYS[1]
----当前线程标识
--local threadId = ARGV[1]

--获取锁中的线程标识
--local id = redis.call('get', KEYS[1])

--比较当前线程标识和锁中的线程标识
if(redis.call('get', KEYS[1]) == ARGV[1]) then
    --一致，则删除锁
    redis.call('del', KEYS[1])
end
return 0
