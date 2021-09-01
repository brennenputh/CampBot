import discord
from discord.ext import commands
import os, random, re
from datetime import datetime
from fuzzywuzzy import fuzz

intents = discord.Intents.default()
intents.members = True

command_prefix='$'
bot = commands.Bot(command_prefix=command_prefix, intents=intents)
TOKEN = "Nzk0NjYxMzMwNzExNjA5MzU1.X--ECQ.dOy1Bi5v8is426W9JgLA2xb3EKE"

animal_categories = [
        "cat", 
        "dog",
        "baby",
        "robot",
        "staff",
        "fish"
]

quotes_file = "quotes.txt"

@bot.event
async def on_ready():
    print("Ready")


@bot.event
async def on_message(message):
    if message.author == bot.user:
        return
    
    links = re.findall(r'https?:\/\/(?:canary\.)?discord\.com\S+', message.content)
    for link in links:
        link_split = link.split('/')
        server_id = int(link_split[4])
        channel_id = int(link_split[5])
        msg_id = int(link_split[6])
        server = bot.get_guild(server_id)
        channel = server.get_channel(channel_id)
        fetched_message = await channel.fetch_message(msg_id)
        embed = discord.Embed(title="Quoted from " + fetched_message.author.name, description=fetched_message.content)
        if len(fetched_message.content) != 0:
            await message.channel.send(embed=embed)

    quote_inlines = re.findall(r'\{#(\d+)\}', message.content)
    for quote_number in quote_inlines:
        ctx = await bot.get_context(message)
        await quote(ctx, quote_number) 

    # Use the categories for commands
    if message.content.startswith(command_prefix):
        text = message.clean_content[1:]
        if text in animal_categories:
            try:
                pic = random.choice(os.listdir("./pictures/" + text))
                picFile = discord.File("./pictures/" + text + "/" + pic)
                await message.channel.send(content="Here's your " + text + "!", file=picFile)
            except:
                await message.channel.send(content="Error occurred... are there any pictures in this category?")
            return

    # Lets commands function
    await bot.process_commands(message)


@bot.command()
async def upload(ctx, category):
    """Upload image(s) to the bot.  Use this command while attaching a photo."""
    if len(ctx.message.attachments) > 0:
        for attachment in ctx.message.attachments:
            now = datetime.now().strftime("%m-%d-%Y-%H-%M-%S")
            await attachment.save("./pictures/" + category + "/" + now + attachment.filename)
            print("Uploaded " + now + attachment.filename + " to category " + category)
        await ctx.send("Picture(s) have arrived at destination.")
    else:
        await ctx.send("Upload a picture with your command!")


@bot.command()
async def categories(ctx):
    """Lists all available categories for the bot.  Summon a image from any of these with `$<category>`."""
    categories = ""
    for i in animal_categories:
        categories = categories + "`" + i + "`\n"
    embed = discord.Embed(title="Categories:", description=categories)
    await ctx.send(embed=embed)


@bot.command()
async def cp(ctx, content, author):
    """Alias to createquote."""
    await createquote(ctx, content, author)


@bot.command()
async def cq(ctx, content, author):
    """Alias to createquote."""
    await createquote(ctx, content, author)


@bot.command()
async def createquote(ctx, content, author):
    """Creates a quote."""
    with open(quotes_file, "r") as fp:
        line = fp.readlines()[len(fp.readlines())-1]
    quote_number = str(file_len(quotes_file)+2)
    final = quote_number + " " + content + " - " + author
    final = final.replace('\n', '')
    with open(quotes_file, "a") as fp:
        fp.write(final + '\n')
    embed = discord.Embed(title="Quote added:", description="#" + quote_number + ": " + content + " - " + author, colour=discord.Colour.green())
    await ctx.send(embed=embed)
    if int(quote_number) % 100 == 0:
        quote_file = discord.File("./quotes.txt")
        embed = discord.Embed(title="You have reached a multiple of 100 quotes.", description="This calls for a prize!")
        await ctx.send(embed=embed, file=quote_file)


@bot.command()
async def quote(ctx, number):
    """Get a quote from the bot, indexed by number."""
    with open(quotes_file, "r") as fp:
        for line in fp:
            if line.split(" ")[0] == number:
                embed = quote_embed(line)
                await ctx.send(embed=embed)

@bot.command()
async def randomquote(ctx):
    """Grabs a random quote."""
    with open(quotes_file, "r") as fp:
        lines = fp.read().splitlines()
        quote = random.choice(lines)
        embed = quote_embed(quote)
        await ctx.send(embed=embed)


def quote_embed(quote_string):
    parts = quote_string.split(" ")
    number = parts[0]
    author = quote_string[quote_string.rindex("-")+2:]
    quote_content = quote_string[len(number):quote_string.rindex("-")-1]
    return discord.Embed(title="Quote #" + number, description = quote_content + " - " + author, colour=discord.Colour.green())
    

@bot.command()
async def search(ctx, content):
    """Searches quote index for the query."""
    result = []
    result_int = []
    matches = 0
    with open(quotes_file, "r") as fp:
        for line in fp:
            # Use fuzzy with ratio, ratio under 50 becomes a problem, pretty sure the sorting is broken
            ratio = fuzz.token_set_ratio(line, content)
            if(ratio > 50):
                matches += 1
                result.append("" + str(matches) + ". #" + line + "\n")
                result_int.append(ratio)
    if(matches == 0):
        await ctx.send("No matches.")
        return
    sorted_results = [result for _,result in sorted(zip(result_int, result))]
    sorted_results = ''.join(sorted_results[:10])
    embed = discord.Embed(title="Matches:", description=sorted_results, colour=discord.Colour.green())
    await ctx.send(embed=embed)


@bot.command()
async def count(ctx, content):
    """Gets the number of occurances of the query in quotes."""
    count = 0
    with open(quotes_file, "r") as fp:
        for line in fp:
            if content.lower() in line.lower():
                count += 1
    if(count == 0):
        await ctx.send("No matches.")
        return
    embed = discord.Embed(title="Number of matches for \"" + content + "\": " + str(count))
    await ctx.send(embed=embed)


@bot.command()
async def info(ctx, member):
    if(member.startswith("<@!")):
        member = member.replace("<@!", "").replace(">", "")
    if not is_number(member):
        user = ctx.guild.get_member_named(member)
    else:
        user = ctx.guild.get_member(int(member))
    if user == None:
        await ctx.send("Couldn't find user.  Check that you typed the username right.")
        return
    description = "User ID: " + str(user.id) + "\nUser Nickname: " + user.display_name + "\nAvatar URL: " + str(user.avatar_url) + "\nDate of account creation: " + str(user.created_at) + "\nRoles: "
    for role in user.roles:
        if not role.is_default():
            description = description + " " + role.name
    embed = discord.Embed(title=user.name, description=description, colour=user.color)
    embed.set_thumbnail(url=str(user.avatar_url))
    await ctx.send(embed=embed)


def file_len(fname):
    i = 0
    with open(fname) as fp:
        for i, l in enumerate(fp):
            pass
    return i


def is_number(s):
    try:
        float(s)
        return True
    except ValueError:
        return False

bot.run(TOKEN);
