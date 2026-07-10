with open('kayan_repo/backend/src/app.module.ts', 'r') as f:
    content = f.read()

content = content.replace(
'''    consumer
      .apply(HmacMiddleware)
      // .exclude({ path: 'auth/(.*)', method: RequestMethod.ALL }) // Example
      .forRoutes({ path: 'secure-endpoints/*', method: RequestMethod.ALL }); 
      // User requested "أضف Middleware للـ HMAC request signing في NestJS"
      // Without specifying a route, I will apply it globally or to a specific route. Let's apply to all except auth.''',
'''    consumer
      .apply(HmacMiddleware)
      .exclude(
        { path: 'auth/(.*)', method: RequestMethod.ALL },
        { path: 'health', method: RequestMethod.ALL }
      )
      .forRoutes('*');'''
)

with open('kayan_repo/backend/src/app.module.ts', 'w') as f:
    f.write(content)
