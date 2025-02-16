# S'ash - An Embeddable Language

⚠️ Warning: This language is still in its early development.

S'ash is a lightweight, embeddable programming language designed with simplicity and flexibility in mind. It offers a concise and unified syntax while maintaining clarity and expressiveness.

## Getting Started

#### 1. Add JitPack Repository

In your `settings.gradle.kts`, add the following to include the JitPack repository:

```Kotlin
dependencyResolutionManagement {
   repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
   repositories {
      google()
      mavenCentral()
      maven { url = URI("https://jitpack.io") }
   }
}
```

Or your `settings.gradle`:

```Groovy
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

#### 2. Add Sash Dependency

In your module-level `build.gradle.kts`, add the Sash dependency:

```Kotlin
dependencies {
  implementation("com.github.saidooubella:sash:LAST-COMMIT-HASH")
}
```

Or your module-level `build.gradle`:

```Groovy
dependencies {
  implementation "com.github.saidooubella:sash:LAST-COMMIT-HASH"
}
```

Make sure to replace `LAST-COMMIT-HASH` with the hash of the latest commit from the `dev` branch.

#### 3. Use Sash in Your Code

Now, you can start using Sash in your project. Here’s a basic example:

```Kotlin
private suspend fun compile(source: String) = withContext(Dispatchers.IO) {

    val positionBuilder = MutablePositionBuilder()
    val diagnostics = DiagnosticsReporter("<playground>")

    val program = MutableIntInput(StringProvider(source))
        .observer { old, new -> positionBuilder.advance(old, new) }
        .map { input -> RawTokensProvider(input, TokenizerContext(positionBuilder, diagnostics)) }
        .map { input -> TokensProvider(input) }
        .use { input -> Parse(input, ParserContext(diagnostics)) }
        .let { program -> Refine(program, RefinerContext(diagnostics)) }

    diagnostics.build().map { it.formattedMessage }.toImmutableList() to program
}

fun main() {
    val (diagnostics, program) = compile("""println("Hello, world!");""")
    Evaluator(program, builtInEnvironment())
}
```

For a complete example of using Sash, check out the (Sash Run
[https://github.com/saidooubella/android-sash-run]

## Features

-   **Embeddable**: Seamlessly integrates into applications, offering flexibility for various use cases.
-   **Minimalistic Syntax**: Inspired by modern programming paradigms, prioritizing simplicity, readability, and developer productivity.
-   **Strong Type System**: Supports a statically-typed system that ensures type safety and reduces runtime errors.

## Language Overview

### Hello world!

Getting started with Sash is simple and intuitive. Here’s a basic example of printing "Hello, world!":

```Sash
println("Hello, world!");
```

### Comments

Sash supports two types of comments:

#### Single-Line Comments

Single-line comments begin with `//` and extend to the end of the line.

```Sash
// This is a single-line comment
def name = "Said Oubella"; // Inline comment
``` 

#### Multi-Line Comments

Multi-line comments start with `/*` and end with `*/`. They can span multiple lines.

```Sash
/*
  This is a multi-line comment
  that spans multiple lines.
*/
```

#### Nested Multi-Line Comments

Multi-line comments can be nested within each other, allowing you to comment out sections of code that already contain multi-line comments. This is particularly useful when temporarily disabling large blocks of code.

```Sash
/*
  Outer comment
  /* Nested comment */
  Continue outer comment
*/
```

### Types

Sash supports the following core types:

-   **Unit**: Represents the default return value for functions. It is typically used for functions that do not return any meaningful value.
-   **Nothing**: Denotes an expression that will never produce a value, often used to indicate unreachable code or functions that do not terminate.
-   **Boolean**: A type representing a binary value, which can either be `true` or `false`.
-   **String**: A sequence of characters enclosed in double quotes (`" "`), used to represent text.
-   **Float**: A 32-bit floating-point number, used for representing decimal values with single precision.
-   **Int**: A 32-bit signed integer, used for whole numbers.

### Literals

In Sash, **literals** are fixed values used directly in the code. They are not computed and remain constant throughout the program.

-   **String Literal**: A sequence of characters enclosed in double quotes (`"Hello, world!"`).
-   **Integer Literal**: A whole number composed of one or more digits (`70803`).
-   **Decimal Literal**: A floating-point number written with a decimal point (`70820.03`).
-   **Boolean Literal**: Represents truth values, either `true` or `false`.

### Operations

In Sash, operations are performed on values of specific types, and certain operations are only allowed between compatible types. Below are the supported operations for different types:

⚠️ Warning: Note that these operations only works when both their operands are of the same type, i.e. `12.0 + 12` is considered a type error.

#### Arithmetic Operations

| **Type**            | **Operation**  | **Symbol** | **Example**   |
|---------------------|----------------|------------|---------------|
| **Integer/Decimal** | Addition       | `+`        | `5 + 10`      |
|                     | Subtraction    | `-`        | `10.9 - 3.43` |
|                     | Multiplication | `*`        | `4 * 2`       |
|                     | Division       | `/`        | `10 / 2`      |
|                     | Modulo         | `%`        | `10.0 % 3.0`  |

#### Comparison Operations

| **Type**            | **Operation**            | **Symbol** | **Example**           |
|---------------------|--------------------------|------------|-----------------------|
| **Integer/Decimal** | Equality                 | `==`       | `5 == 5`              |
|                     | Inequality               | `!=`       | `5 != 6`              |
|                     | Greater Than             | `>`        | `10 > 5`              |
|                     | Less Than                | `<`        | `5 < 10`              |
|                     | Greater Than or Equal To | `>=`       | `10 >= 5`             |
|                     | Less Than or Equal To    | `<=`       | `5 <= 10`             |
| **Any Type**        | Equality                 | `==`       | `"apple" == "apple"`  |
|                     | Inequality               | `!=`       | `"apple" != "banana"` |

#### Logical Operations

| **Type**    | **Operation** | **Symbol** | **Example**       |
|-------------|---------------|------------|-------------------|
| **Boolean** | Logical AND   | `&&`       | `true && false`   |
|             | Logical OR    | `\|\|`     | `true \|\| false` |
|             | Logical NOT   | `!`        | `!true`           |

### Control Flow

statements allow you to dictate the flow of execution in your Sash program. Below are the primary control flow structures.

#### **If-Else**

 The `if` statement allows you to execute a block of code conditionally. You can also include an `else` branch to handle cases when the condition is not met.
 
```Sash
if dirty {
    validate();
}

if x > 10 {
    println("x is greater than 10");
} else {
    println("x is 10 or less");
}
```

You can chain multiple `if` expressions/statements.

```Sash
if x > 10 {
    println("x is greater than 10");
} else if x == 10 {
    println("x is exactly 10");
} else {
    println("x is less than 10");
}
```

It can also be used as an expression to return a value based on the condition.

You can yield values directly from an `if` expression by omitting the semicolon of the last expression.

```Sash
// Assuming x > 10 this will print "Greater than 10"
println(if x > 10 { "Greater than 10" } else { "10 or less" });
```

#### **While**

The `while` loop allows you to execute a block of code repeatedly as long as a condition is true.

```Sash
while x < 5 {
    println(x);
    x = x + 1;
}
```

#### **Break**

The `break` statement is used to exit from a loop prematurely.

```Sash
while true {
    println("This will run forever unless broken");
    break; // Exit the loop
}
```

#### **Continue**

The `continue` statement skips the current iteration of the loop and moves to the next iteration.

```Sash
while i < 5 {
    if i == 3 {
        continue; // Skip when i equals 3
    }
    println(i);
    i = i + 1;
}
```

### Definitions

In Sash, **definitions** are a unified way to bind values and create variables, functions, and even types. You can use them to define nearly anything in your program, making the language concise and flexible.

Example: Basic Definitions

```Sash
def name = "Said Oubella";
def greeting = "Hello";

println(greeting + ", " + name + "!");
```

Here, `name` and `greeting` are defined as string values. The types are inferred automatically, making the language easier to work with.

#### Type Inference and Explicit Types

Definitions automatically infer types for variables when possible. You can also specify types explicitly if needed.

```Sash
def anInt = 12;
def yetAnotherInt: Int = 12;
```

#### Defining Types

Sash allows you to define complex types such as records and enums using definitions, though some features (like member access) are still being implemented.

⚠️ **Warning**: Records, enums, and member access are not fully supported yet.

```Sash
def Person = record { name: String, age: Int };
def someone = Person("Someone", 68);

println(someone);
```

#### Defining Functions

Functions can also be defined with the same `def` keyword, allowing for functional programming constructs to be used in a clear and readable manner.

```Sash
def add = { lhs: Int, rhs: Int ->
    return lhs + rhs;
};

def main = {
    println(add(1, 2));
};

main();
```

##### Implicit Returns

Like `if` expressions, functions can omit the `return` keyword and semicolon if the last expression produces a value:

```Sash
def add = { lhs: Int, rhs: Int -> lhs + rhs };
println(add(3, 4));
```

##### First-Class Functions

In Sash, functions are first-class citizens, meaning they can be bound to definitions, passed as arguments, and returned from other functions.

##### Function Types

Functions have a specific type notation using the `->` symbol. The syntax follows this pattern:

`(paramType1, paramType2, ...) -> returnType`

Examples:

```Sash
def add: (Int, Int) -> Int = { a: Int, b: Int -> a + b };
println(add(3, 5));
```

Function parameter type annotations can be omitted when the surrounding context, such as explicit type annotations or usage, provides enough information for the compiler to infer them.

```Sash
def greet: (String) -> Unit = { name ->
    println("Hello, " + name);
};

greet("Sash");
```

##### Trailing Lambdas

If a function takes another function as its last parameter, you can define it outside the parentheses:

```Sash
def repeat = { times: Int, block: (Int) -> Unit ->  
    def mut index = 0;  
    while index < times {  
        block(index);  
        index = index + 1;  
    }  
};  
  
repeat(6) { index ->  
    println(index);  
};
```

#### Generic Definitions

Sash supports generics, allowing both functions and types to be defined with type parameters for greater flexibility while maintaining type safety.

##### Generic Functions

Functions can be parameterized using type parameters in square brackets (`[T]`). This allows them to work with multiple types while preserving strong typing.

```Sash
def identity[T] = { value: T -> value };

println(identity(42));      // 42
println(identity("Hello")); // "Hello"
```

When the type can be inferred from the argument, it does not need to be explicitly specified.

##### Generic Types

Records and other definitions can also be generic, making them adaptable to different types while keeping their structure intact.

⚠️ **Warning**: Records, enums, and member access are not fully supported yet.

```Sash
def Box[T] = record { value: T }

def intBox = Box(10);
def stringBox = Box("Hello");
```

By leveraging generic definitions, Sash enables expressive and reusable abstractions without sacrificing type safety.

##### Restriction

**Generics can only be used with functions and type definitions** – You cannot use generics with mutable definitions.

#### Mutable Definitions

By default, definitions in Sash are immutable, meaning their values cannot be reassigned after initialization. However, if you need to update a value, you can use `def mut` to declare a mutable definition.

```Sash
def mut counter = 0;
counter = counter + 1; // Allowed because counter is mutable

println(counter); // 1
```

##### Immutability by Default

Sash enforces immutability unless explicitly stated with `def mut`, promoting safer and more predictable code. This helps prevent unintended modifications and makes it easier to reason about program state.

##### Restriction

**Generics cannot be used with mutable definitions** – You cannot define a mutable variable with a generic type.

### Discard Non-Unit Expressions

In Sash, you can discard the result of non-unit expressions using the discard statement. This allows you to evaluate an expression without retaining its result.

**Example**:
```Sash
_ = 42 + 10;
```

This tells the compiler to evaluate the expression but ignore its result.

**Important Notes**:

- Expressions must have a non-`Unit` type when discarded;
- Discarding a `Unit` expression results in a compile-time error.
- Having an expression with a non-`Unit` type without discarding it is also a compile-time error.

## Grammar

### Notation

| Symbol         | Description                                 |
|----------------|---------------------------------------------|
| `( .. )`       | Group – Groups elements together            |
| `? .. ?`       | Description – Provides additional info      |
| `'..'`         | Terminal – Fixed symbol                     |
| `[a-z][a-z-]*` | Non-terminal – Expandable symbol            |
| `..=`          | Inclusive range – Both ends included        |
| `\|`           | Alternatives – Either/or choice             |
| `*`            | Zero or more – Repeats zero or more times   |
| `+`            | One or more – Repeats one or more times     |
| `?`            | Optional – Element may be present or absent |
| `~`            | Negation – Excludes an element              |

### Statements

```
statement            = definition
                     | continue
                     | discard
                     | return
                     | break
                     | while
                     | yield
                     | unit
                     ;
```

```
discard              = _ = expression ';'
                     ;
```

```
unit                 = expression ';'
                     ;
```

```
yield                = expression
                     ;
```

```
definition           = 'def' 'mut'? identifier type-params? type-annotation? '=' initializer ';'
                     ;
```

```
initializer          = record ( '{' fields '}' )?
                     | enum '{' enum-entries '}'
                     | expression
                     ;
```

```
enum-entries         = enum-entry ( ',' enum-entry )* ','?
                     ;
```

```
enum-entry           = identifier ( '{' fields '}' )?
                     ;
```

```
type-params          = '[' identifier ( ',' identifier )* ','? ']'
                     ;
```

```
while                = 'while' expression control-body
                     ;
```

```
return               = 'return' ( ';' | expression ';' )
                     ;
```

```
continue             = 'continue' ';'
                     ;
```

```
break                = 'break' ';'
                     ;
```

### Expressions

```
expression           = assignment
                     ;
```

```
assignment           = disjunction ( '=' expression )?
                     ;
```

```
disjunction          = conjunction ( '||' conjunction )*
                     ;
```

```
conjunction          = equality ( '&&' equality )*
                     ;
```

```
equality             = comparison ( ( '==' | '!=' ) comparison )*
                     ;
```

```
comparison           = additive ( ( '>' | '<' | '>=' | '<=' ) additive )*
                     ;
```

```
additive             = multiplicative ( ( '+' | '-' ) multiplicative )*
                     ;
```

```
multiplicative       = prefix ( ( '*' | '/' | '%' ) prefix )*
                     ;
```

```
prefix               = ( '!' | '+' | '-' ) prefix
                     | postfix
                     ;
```

```
postfix              = primary type-args? ( value-args* | value-args function | function )
                     ;
```

```
value-args           = '(' ( expression ( ',' expression )* ','? )? ')'
                     ;
```

```
primary              = boolean
                     | decimal
                     | function
                     | identifier
                     | integer
                     | string
                     | if
                     ;
```

```
if                   = if-branch else-branch?
                     ;
```

```
if-branch            = 'if' expression control-body
                     ;
```

```
else-branch          = 'else' ( if | control-body )
                     ;
```

```
boolean              = 'true'
                     | 'false'
                     ;
```

```
integer              = digit+
                     ;
```

```
decimal              = digit+ '.' digit+
                     ;
```

```
string               = '"' string-part* '"'
                     ;
```

```
string-part          = ~( '"' | '\r\n' | '\r' | '\n' )
                     | '\' ( '\' | 'r' | 'n' | 't' | 'b' | '"' | '\r\n' | '\r' | '\n' )
                     | '\u' hex-digit hex-digit hex-digit hex-digit
                     ;
```

```
function             = '{' function-params? statement* '}'
                     ;
```

```
function-params      = function-param ( ',' function-param )* ','? '->'
                     ;
```

```
function-param       = identifier type-annotation?
                     ;
```

### Types

```
type                 = function-type
                     | simple-type
                     ;
```

```
function-type        = '(' ( type ( ',' type )* ','? )? ')' '->' type
                     ;
```

```
simple-type          = identifier type-args?
                     ;
```

### Common

```
fields               = field ( ',' field )* ','?
                     ;
```

```
field                = identifier type-annotation
                     ;
```

```
type-args            = '[' type ( ',' type )* ','? ']'
                     ;
```

```
type-annotation      = ':' type
                     ;
```

```
control-body         = '{' statement* '}'
                     ;
```

```
digit                = '0' ..= '9'
                     ;
```

```
hex-digit            = digit
                     | 'A' ..= 'F'
                     | 'a' ..= 'f'
                     ;
```

```
identifier           = identifier-start identifier-rest*
                     ;
```

```
identifier-start     = unicode-letter
                     | '_'
                     ;
```

```
identifier-rest      = identifier-start
                     | unicode-digit
                     ;
```

```
unicode-letter       = ? unicode-class-lu ?
                     | ? unicode-class-ll ?
                     | ? unicode-class-lt ?
                     | ? unicode-class-lm ?
                     | ? unicode-class-lo ?
                     ;
```

```
unicode-digit        = ? unicode-class-nd ?
                     | ? unicode-class-nl ?
                     ;
```
