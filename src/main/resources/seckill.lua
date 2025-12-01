--优惠卷id
local voucherId = ARGV[1]
--用户id
local userId = ARGV[2]
--订单id
local orderId = ARGV[3]
--库存key
local stockKey = 'seckill:stock:' .. voucherId
--订单key
local orderKey = 'seckill:order:' .. voucherId

if (tonumber(redis.call('get', stockKey)) <= 0) then
    return 1
end
if (redis.call('sismember', orderKey, userId) == 1) then
    return 2
end
--库存-1
redis.call('incrby', stockKey, -1)
--添加到订单
redis.call('sadd', orderKey, userId)
--发送消息到队列中 xdd stream.orders      *          k1 v1 k2 v2.....
--                       ||           ||
--                   队列的名称   (表示自动生成消息id)
redis.call('xadd','stream.orders','*','voucherId',voucherId,'userId',userId,'id',orderId)
return 0
