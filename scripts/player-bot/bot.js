// Headless player for smoke tests. Joins an offline-mode server, prints every chat
// message it receives as "CHAT <text>", and runs each line appended to /tmp/commands
// as a chat command, so a test can interleave RCON changes with player actions.
//
// usage: node bot.js <host> [minecraft-version]
const fs = require('fs');
const mineflayer = require('mineflayer');

const host = process.argv[2];
const version = process.argv[3] || false;
const commands = '/tmp/commands';

fs.writeFileSync(commands, '');

const bot = mineflayer.createBot({ host, port: 25565, username: 'PexBot', auth: 'offline', version });

let consumed = 0;

bot.once('spawn', () => {
    console.log('SPAWNED');

    setInterval(() => {
        const lines = fs.readFileSync(commands, 'utf8').split('\n').filter((line) => line.length > 0);

        for (; consumed < lines.length; consumed++) {
            console.log('RUN ' + lines[consumed]);
            bot.chat(lines[consumed]);
        }
    }, 250);
});

bot.on('message', (message) => console.log('CHAT ' + message.toString()));

// Chat plugins can send chat that this client cannot parse ("unknown chat format code");
// stay connected rather than crash.
process.on('uncaughtException', (error) => console.log('ERROR ' + error.message));
bot.on('kicked', (reason) => console.log('KICKED ' + JSON.stringify(reason)));
bot.on('error', (error) => console.log('ERROR ' + error.message));
bot.on('end', () => {
    console.log('ENDED');
    process.exit(0);
});
