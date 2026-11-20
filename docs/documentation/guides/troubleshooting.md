# Troubleshooting

## Permission Not Working

1. **Check Precedence**: Ensure no denying permission is overriding your allowance.
2. **Check Context**: Verify if the permission is limited to a specific world or server.
3. **Trace Permission**: Use `/pex user <user> check <permission>` to see why a permission is resolved a certain way.

## Database Connection Failed

1. **Verify Credentials**: Double-check `database.yml`.
2. **Check Network**: Ensure the database server is reachable.
3. **Logs**: Check the server console for specific SQL errors.

## Redis Sync Issues

1. **Enable Debug**: Turn on debug mode in `config.yml`.
2. **Check Channel**: Ensure all servers are using the same Redis channel.
