# Commands

## Key 
| Symbol      | Meaning                        |
| ----------- | ------------------------------ |
| [Argument]  | Argument is not required.      |

## Pictures
| Commands   | Arguments | Description                                                                  |
| ---------- | --------- | ---------------------------------------------------------------------------- |
| categories |           | Get the list of available categories of pictures.  Example: &categories      |
| post       | Any       | Get a picture.  Example: &post staff                                         |
| upload     | Any       | Upload a picture to the bot.  Expects an attachment.  Example: &upload staff |

## Quotes
| Commands            | Arguments        | Description                                                                                                               |
| ------------------- | ---------------- | ------------------------------------------------------------------------------------------------------------------------- |
| createquote, cq, cp | Quote, Quote     | Create a quote.  Takes two quote arguments, Content and Author.  Example: &createquote "content" "author"                 |
| quote               | Integer          | Get a quote.  Quotes are found by number.  Example: &quote 1                                                              |
| randomquote         |                  | Get a random quote.  Example: &randomquote                                                                                |
| search              | Quote, [Boolean] | Search for a phrase in the quotes file.  Optional second argument to search by author name.  Example: &search "foo" false |

## Utility
| Commands | Arguments | Description          |
| -------- | --------- | -------------------- |
| Help     | [Command] | Display a help menu. |

