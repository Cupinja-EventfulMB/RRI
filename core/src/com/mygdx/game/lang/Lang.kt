package com.mygdx.game.lang

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.mygdx.game.utils.MapRasterTiles
import com.mygdx.game.utils.ZoomXY
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.InputStream
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

const val ERROR_STATE = 0

const val EOF_SYMBOL = -1
const val SKIP_SYMBOL = 0
const val NEWLINE = '\n'.code
const val INT = 1
const val VAR = 2
const val PLUS = 3
const val MINUS = 4
const val LPAREN = 5
const val RPAREN = 6
const val LSPAREN = 7
const val RSPAREN = 8
const val LCPAREN = 9
const val RCPAREN = 10
const val ASSIGN = 11
const val CITY = 12
const val STREET = 13
const val POINT = 14
const val INSTITUTION = 15
const val SQUARE = 16
const val STATUE = 17
const val STRING = 18
const val BLOCK = 19
const val BEND = 20
const val LINE = 21
const val COMMA = 22
const val ADDRESS = 23
const val DEC_STRING = 24
const val DEC_INT = 25
const val DEC_COORD = 26
const val EVENTS = 27
const val FST = 28
const val SND = 29
const val LAKE = 30
const val CIRCLE = 31
const val IF = 32
const val ELSE = 33
const val EQUALS = 34
const val SMALLER = 35
const val BIGGER = 36
const val NOTEQUAL = 37
const val DOUBLE = 38
const val DEC_DOUBLE = 39
const val CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
const val STRING_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ 0123456789.,"
const val NUMBER_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"


interface DFA {
    val states: Set<Int>
    val alphabet: IntRange
    fun next(state: Int, code: Int): Int
    fun symbol(state: Int): Int
    val startState: Int
    val finalStates: Set<Int>
}

object ForForeachFFFAutomaton: DFA {
    override val states = (1 .. 120).toSet()
    override val alphabet = 0 .. 255
    override val startState = 1
    override val finalStates = setOf(2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 17, 23, 28, 39, 44, 48, 54, 57, 61, 63, 64, 71, 74, 75, 84, 89, 92, 94, 97, 101, 102, 105, 106, 107, 108, 110, 112, 118, 119, 120)

    private val numberOfStates = states.max() + 1 // plus the ERROR_STATE
    private val numberOfCodes = alphabet.max() + 1 // plus the EOF
    private val transitions = Array(numberOfStates) {IntArray(numberOfCodes)}
    private val values = Array(numberOfStates) {SKIP_SYMBOL}

    private fun setTransition(from: Int, chr: Char, to: Int) {
        transitions[from][chr.code + 1] = to // + 1 because EOF is -1 and the array starts at 0
    }

    private fun setTransition(from: Int, code: Int, to: Int) {
        transitions[from][code + 1] = to
    }

    private fun setSymbol(state: Int, symbol: Int) {
        values[state] = symbol
    }

    override fun next(state: Int, code: Int): Int {
        assert(states.contains(state))
        assert(alphabet.contains(code))
        return transitions[state][code + 1]
    }

    override fun symbol(state: Int): Int {
        assert(states.contains(state))
        return values[state]
    }
    init {
        // int [0-9]+
        for (a in '0' .. '9'){
            setTransition(1,a,2)
            setTransition(2,a,2)
        }
        setSymbol(2,INT)

        // variable [a-zA-Z]+[0-9]*
        for(character in CHARS){
            setTransition(1, character, 3)
        }
        for(character in CHARS){
            setTransition(3, character, 3)
        }
        for(digit in '0'..'9'){
            setTransition(3, digit, 4)
        }
        for(digit in '0'..'9'){
            setTransition(4, digit, 4)
        }
        setSymbol(3, VAR)
        setSymbol(4,VAR)

        // plus
        setTransition(1,'+',5)
        setSymbol(5,PLUS)

        // minus
        setTransition(1,'-',6)
        setSymbol(6,MINUS)

        // lparen
        setTransition(1,'(',7)
        setSymbol(7,LPAREN)

        // rparen
        setTransition(1,')',8)
        setSymbol(8,RPAREN)

        // lsparen
        setTransition(1,'[',9)
        setSymbol(9,LSPAREN)

        // rsparen
        setTransition(1,']',10)
        setSymbol(10,RSPAREN)

        // lcparen
        setTransition(1,'{',11)
        setSymbol(11,LCPAREN)

        // rcparen
        setTransition(1,'}',12)
        setSymbol(12,RCPAREN)

        // assign
        setTransition(1,'=',13)
        setSymbol(13,ASSIGN)

        // City
        setTransition(1, 'c', 14)
        setTransition(14, 'i', 15)
        setTransition(15, 't', 16)
        setTransition(16, 'y', 17)

        for(character in CHARS){
            if ((character != 'i') && (character != 'o') && (character != 'r')) setTransition(14, character, 3)
            if (character != 't') setTransition(15, character, 3)
            if (character != 'y') setTransition(16, character, 3)
            if (character != ' ') setTransition(17, character, 3)
        }
        setSymbol(17, CITY)

        // Street
        setTransition(1, 's', 18)
        setTransition(18, 't', 19)
        setTransition(19, 'r', 20)
        setTransition(20, 'e', 21)
        setTransition(21, 'e', 22)
        setTransition(22, 't', 23)

        for(character in CHARS){
            if ((character != 't') && (character != 'q') && (character != 'n')) setTransition(18, character, 3)
            if ((character != 'r') && (character != 'a')) setTransition(19, character, 3)
            if ((character != 'e') && (character !='i')) setTransition(20, character, 3)
            if (character != 'e') setTransition(21, character, 3)
            if (character != 't') setTransition(22, character, 3)
            if (character != ' ') setTransition(23, character, 3)
        }
        setSymbol(23, STREET)

        // Point
        setTransition(1, 'p', 24)
        setTransition(24, 'o', 25)
        setTransition(25, 'i', 26)
        setTransition(26, 'n', 27)
        setTransition(27, 't', 28)

        for(character in CHARS){
            if (character != 'o') setTransition(24, character, 3)
            if (character != 'i') setTransition(25, character, 3)
            if (character != 'n') setTransition(26, character, 3)
            if (character != 't') setTransition(27, character, 3)
            if (character != ' ') setTransition(28, character, 3)
        }
        setSymbol(28, POINT)

        // Institution
        setTransition(1, 'i', 29)
        setTransition(29, 'n', 30)
        setTransition(30, 's', 31)
        setTransition(31, 't', 32)
        setTransition(32, 'i', 33)
        setTransition(33, 't', 34)
        setTransition(34, 'u', 35)
        setTransition(35, 't', 36)
        setTransition(36, 'i', 37)
        setTransition(37, 'o', 38)
        setTransition(38, 'n', 39)

        for(character in CHARS){
            if ((character != 'n') && (character != 't') && (character != 'f')) setTransition(29, character, 3)
            if (character != 's') setTransition(30, character, 3)
            if (character != 't') setTransition(31, character, 3)
            if (character != 'i') setTransition(32, character, 3)
            if (character != 't') setTransition(33, character, 3)
            if (character != 'u') setTransition(34, character, 3)
            if (character != 't') setTransition(35, character, 3)
            if (character != 'i') setTransition(36, character, 3)
            if (character != 'o') setTransition(37, character, 3)
            if (character != 'n') setTransition(38, character, 3)
            if (character != ' ') setTransition(39, character, 3)
        }
        setSymbol(39, INSTITUTION)

        // Square
        setTransition(18, 'q', 40)
        setTransition(40, 'u', 41)
        setTransition(41, 'a', 42)
        setTransition(42, 'r', 43)
        setTransition(43, 'e', 44)

        for(character in CHARS){
            if (character != 'u') setTransition(40, character, 3)
            if (character != 'a') setTransition(41, character, 3)
            if (character != 'r') setTransition(42, character, 3)
            if (character != 'e') setTransition(43, character, 3)
            if (character != ' ') setTransition(44, character, 3)
        }
        setSymbol(44, SQUARE)

        // Statue
        setTransition(19, 'a', 45)
        setTransition(45, 't', 46)
        setTransition(46, 'u', 47)
        setTransition(47, 'e', 48)

        for(character in CHARS){
            if (character != 't') setTransition(45, character, 3)
            if (character != 'u') setTransition(46, character, 3)
            if (character != 'e') setTransition(47, character, 3)
            if (character != ' ') setTransition(48, character, 3)
        }
        setSymbol(48, STATUE)

        // Block
        setTransition(1, 'b', 50)
        setTransition(50, 'l', 51)
        setTransition(51, 'o', 52)
        setTransition(52, 'c', 53)
        setTransition(53, 'k', 54)

        for(character in CHARS){
            if (character != 'l') setTransition(50, character, 3)
            if (character != 'o') setTransition(51, character, 3)
            if (character != 'c') setTransition(52, character, 3)
            if (character != 'k') setTransition(53, character, 3)
            if (character != ' ') setTransition(54, character, 3)
        }
        setSymbol(54, BLOCK)

        // Bend
        setTransition(50, 'e', 55)
        setTransition(55, 'n', 56)
        setTransition(56, 'd', 57)
        for(character in CHARS){
            if (character != 'n') setTransition(55, character, 3)
            if (character != 'd') setTransition(56, character, 3)
            if (character != ' ') setTransition(57, character, 3)
        }
        setSymbol(57, BEND)

        // Line
        setTransition(1, 'l', 58)
        setTransition(58, 'i', 59)
        setTransition(59, 'n', 60)
        setTransition(60, 'e', 61)

        for(character in CHARS){
            if ((character != 'i') && (character != 'a')) setTransition(58, character, 3)
            if (character != 'n') setTransition(59, character, 3)
            if (character != 'e') setTransition(60, character, 3)
            if (character != ' ') setTransition(61, character, 3)
        }
        setSymbol(61, LINE)

        //" element in STRING_CHARS "
        setTransition(1, '"', 62)
        for(character in STRING_CHARS){
            setTransition(62, character, 62)
        }
        setTransition(62,'"', 63)
        setSymbol(63, STRING)

        // comma
        setTransition(1,',',64)
        setSymbol(64, COMMA)

        // address
        setTransition(1, 'a', 65)
        setTransition(65, 'd', 66)
        setTransition(66, 'd', 67)
        setTransition(67, 'r', 68)
        setTransition(68, 'e', 69)
        setTransition(69, 's', 70)
        setTransition(70, 's', 71)

        for(character in CHARS){
            if (character != 'd') setTransition(65, character, 3)
            if (character != 'd') setTransition(66, character, 3)
            if (character != 'r') setTransition(67, character, 3)
            if (character != 'e') setTransition(68, character, 3)
            if (character != 's') setTransition(69, character, 3)
            if (character != 's') setTransition(70, character, 3)
            if (character != ' ') setTransition(71, character, 3)
        }
        setSymbol(71, ADDRESS)

        setTransition(20, 'i', 72)
        setTransition(72, 'n', 73)
        setTransition(73, 'g', 74)

        for(character in CHARS){
            if (character != 'n') setTransition(72, character, 3)
            if (character != 'g') setTransition(73, character, 3)
            if (character != ' ') setTransition(74, character, 3)
        }
        setSymbol(74, DEC_STRING)

        //int
        setTransition(30, 't', 75)

        for(character in CHARS){
            if (character != ' ') setTransition(75, character, 3)
        }
        setSymbol(75, DEC_INT)

        // Coordinate
        setTransition(14, 'o', 76)
        setTransition(76, 'o', 77)
        setTransition(77, 'r', 78)
        setTransition(78, 'd', 79)
        setTransition(79, 'i', 80)
        setTransition(80, 'n', 81)
        setTransition(81, 'a', 82)
        setTransition(82, 't', 83)
        setTransition(83, 'e', 84)

        for(character in CHARS){
            if (character != 'o') setTransition(76, character, 3)
            if (character != 'r') setTransition(77, character, 3)
            if (character != 'd') setTransition(78, character, 3)
            if (character != 'i') setTransition(79, character, 3)
            if (character != 'n') setTransition(80, character, 3)
            if (character != 'a') setTransition(81, character, 3)
            if (character != 't') setTransition(82, character, 3)
            if (character != 'e') setTransition(83, character, 3)
            if (character != ' ') setTransition(84, character, 3)
        }
        setSymbol(84, DEC_COORD)

        // events
        setTransition(1, 'e', 84)
        setTransition(84, 'v', 85)
        setTransition(85, 'e', 86)
        setTransition(86, 'n', 87)
        setTransition(87, 't', 88)
        setTransition(88, 's', 89)

        for(character in CHARS){
            if ((character != 'v') && (character != 'l')) setTransition(84, character, 3)
            if (character != 'e') setTransition(85, character, 3)
            if (character != 'n') setTransition(86, character, 3)
            if (character != 't') setTransition(87, character, 3)
            if (character != 's') setTransition(88, character, 3)
            if (character != ' ') setTransition(89, character, 3)
        }
        setSymbol(89, EVENTS)

        // fst
        setTransition(1, 'f', 90)
        setTransition(90, 's', 91)
        setTransition(91, 't', 92)

        for(character in CHARS){
            if (character != 's') setTransition(90, character, 3)
            if (character != 't') setTransition(91, character, 3)
            if (character != ' ') setTransition(92, character, 3)
        }
        setSymbol(92, FST)

        // snd
        setTransition(18, 'n', 93)
        setTransition(93, 'd', 94)

        for(character in CHARS){
            if (character != 'd') setTransition(93, character, 3)
            if (character != ' ') setTransition(94, character, 3)
        }
        setSymbol(94, SND)

        // Lake
        setTransition(58, 'a', 95)
        setTransition(95, 'k', 96)
        setTransition(96, 'e', 97)

        for(character in CHARS){
            if (character != 'k') setTransition(95, character, 3)
            if (character != 'e') setTransition(96, character, 3)
            if (character != ' ') setTransition(97, character, 3)
        }
        setSymbol(97, LAKE)

        // Circle
        setTransition(15, 'r', 98)
        setTransition(98, 'c', 99)
        setTransition(99, 'l', 100)
        setTransition(100, 'e', 101)

        for(character in CHARS){
            if (character != 'c') setTransition(98, character, 3)
            if (character != 'l') setTransition(99, character, 3)
            if (character != 'e') setTransition(100, character, 3)
            if (character != ' ') setTransition(101, character, 3)
        }
        setSymbol(101, CIRCLE)

        // if
        setTransition(29, 'f', 102)
        for(character in CHARS){
            if (character != ' ') setTransition(102, character, 3)
        }

        setSymbol(102, IF)

        // else
        setTransition(84, 'l', 103)
        setTransition(103, 's', 104)
        setTransition(104, 'e', 105)
        for(character in CHARS){
            if (character != 's') setTransition(103, character, 3)
            if (character != 'e') setTransition(104, character, 3)
            if (character != ' ') setTransition(105, character, 3)
        }

        setSymbol(105, ELSE)

        // equals
        setTransition(13,'=',106)
        setSymbol(106, EQUALS)

        // <
        setTransition(1, '<', 107)
        setSymbol(107, SMALLER)

        // >
        setTransition(1, '>', 108)
        setSymbol(108, BIGGER)

        // !=
        setTransition(1, '!', 109)
        setTransition(109, '=', 110)
        setSymbol(110, NOTEQUAL)

        // Double (int.int)
        setTransition(2, '.', 111)
        for (a in '0' .. '9'){
            setTransition(111,a,112)
            setTransition(112,a,112)
        }
        setSymbol(112, DOUBLE)

        // Double
        setTransition(1, 'd', 113)
        setTransition(113, 'o', 114)
        setTransition(114, 'u', 115)
        setTransition(115, 'b', 116)
        setTransition(116, 'l', 117)
        setTransition(117, 'e', 118)

        for(character in CHARS){
            if (character != 'o') setTransition(113, character, 3)
            if (character != 'u') setTransition(114, character, 3)
            if (character != 'b') setTransition(115, character, 3)
            if (character != 'l') setTransition(116, character, 3)
            if (character != 'e') setTransition(117, character, 3)
            if (character != ' ') setTransition(118, character, 3)
        }
        setSymbol(118, DEC_DOUBLE)

        // ignore [\n\r\t ]+
        setTransition(1,'\n',119)
        setTransition(1,'\r',119)
        setTransition(1,'\t',119)
        setTransition(1,' ',119)
        setSymbol(119,SKIP_SYMBOL)

        // EOF
        setTransition(1,-1,120)
        setSymbol(120,EOF_SYMBOL)

    }
}

data class Token(val symbol: Int, val lexeme: String, val startRow: Int, val startColumn: Int)

class Scanner(private val automaton: DFA, private val stream: InputStream) {
    private var last: Int? = null
    private var row = 1
    private var column = 1

    private fun updatePosition(code: Int) {
        if (code == NEWLINE) {
            row += 1
            column = 1
        } else {
            column += 1
        }
    }

    fun getToken(): Token {
        val startRow = row
        val startColumn = column
        val buffer = mutableListOf<Char>()

        var code = last ?: stream.read()
        var state = automaton.startState
        while (true) {
            val nextState = automaton.next(state, code)
            if (nextState == ERROR_STATE) break

            state = nextState
            updatePosition(code)
            buffer.add(code.toChar())
            code = stream.read()
        }
        last = code

        if (automaton.finalStates.contains(state)) {
            val symbol = automaton.symbol(state)
            return if (symbol == SKIP_SYMBOL) {
                getToken()
            } else {
                val lexeme = String(buffer.toCharArray())
                Token(symbol, lexeme, startRow, startColumn)
            }
        } else {
            throw Error("Invalid pattern at ${row}:${column}")
        }
    }
}

fun name(symbol: Int) =
    when (symbol) {
        INT -> "int"
        VAR -> "variable"
        PLUS -> "plus"
        MINUS -> "minus"
        LPAREN -> "lparen"
        RPAREN -> "rparen"
        LSPAREN -> "lsparen"
        RSPAREN -> "rsparen"
        LCPAREN -> "lcparen"
        RCPAREN -> "rcparen"
        ASSIGN -> "assign"
        CITY -> "city"
        STREET -> "street"
        POINT -> "point"
        INSTITUTION -> "institution"
        SQUARE -> "square"
        STATUE -> "statue"
        STRING -> "string"
        BLOCK -> "block"
        BEND -> "bend"
        LINE -> "line"
        COMMA -> "comma"
        ADDRESS -> "address"
        DEC_STRING -> "stringVar"
        DEC_COORD -> "coordinateVar"
        DEC_INT -> "intVar"
        EVENTS -> "events"
        FST -> "first"
        SND -> "second"
        LAKE -> "lake"
        CIRCLE -> "circle"
        IF -> "if"
        ELSE -> "else"
        EQUALS -> "equals"
        SMALLER -> "smaller"
        BIGGER -> "bigger"
        NOTEQUAL -> "notequal"
        DOUBLE -> "double"
        DEC_DOUBLE -> "doubleVar"
        else -> throw Error("Invalid symbol")
    }

fun printTokens(scanner: Scanner) {
    val token = scanner.getToken()
    if (token.symbol != EOF_SYMBOL) {
        print("${name(token.symbol)}(\"${token.lexeme}\") ")
        printTokens(scanner)
    }
}

data class Coordinate(val name: String, var longtitude: Double, var latitude: Double)

data class Context(val shapeRenderer: ShapeRenderer, val camera: Camera, val beginTile: ZoomXY)

class Parser(private val scanner: Scanner, private var ctx: Context) {
    private var last: Token? = null
    private var geoJSON : String = "{\n\t\"type\": \"FeatureCollection\",\n\t\"features\": ["
    var fileName = "city.geojson"

    private val variableStringMap = mutableMapOf<String, String>()
    private val variableDoubleMap = mutableMapOf<String, Double>()
    private val variableIntMap = mutableMapOf<String, Int>()
    private var variableCoordPair: MutableList<Coordinate> = mutableListOf()

    private val insideVariableStringMap = mutableMapOf<String, String>()
    private val insideVariableDoubleMap = mutableMapOf<String, Double>()
    private val insideVariableIntMap = mutableMapOf<String, Int>()
    private var insideVariableCoordPair: MutableList<Coordinate> = mutableListOf()

    fun parse(): Boolean {
        ctx.shapeRenderer.setProjectionMatrix(ctx.camera.combined);
        ctx.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        ctx.shapeRenderer.color = Color.BLACK
        last = scanner.getToken()
        val status = City()

        /* println("Elements in variableStringMap:")
         for ((key, value) in variableStringMap) {
             println("$key -> $value")
         }

         println("\nElements in variableIntMap:")
         for ((key, value) in variableIntMap) {
             println("$key -> $value")
         }

         println("\nElements in variableDoubleMap:")
         for ((key, value) in variableDoubleMap) {
             println("$key -> $value")
         }

         println("\nElements in variableCoordPair:")
         for (coordinate in variableCoordPair) {
             println(coordinate)
         }

         */
        var file = File(fileName)
        file.createNewFile()
        geoJSON = geoJSON.substring(0, geoJSON.length-2)
        geoJSON += "\n\t]\n}"
        file.writeText(geoJSON)
        ctx.shapeRenderer.end();
        translateGeoJsonFromFile("city.geojson")
        return when (last?.symbol) {
            EOF_SYMBOL -> status
            else -> false
        }
    }

    // City ::= city string { Expression }
    fun City(): Boolean {
        return recognizeTerminal(CITY) && recognizeTerminal(STRING) && recognizeTerminal(LCPAREN) && Expression() && recognizeTerminal(RCPAREN)
    }

    // Expression ::= Operations MandatoryElementsList AdditionalElementsList
    fun Expression(): Boolean {
        return Operations(true) && MandatoryElementsList() && AdditionalElementsList()
    }

    // Operations ::= Operation Operations | e

    fun Operations(status: Boolean): Boolean {
        if (Operation(status) && Operations(status))
            return true
        else return true
    }

    // Operation ::= AssignString | AssignInt | AssignCoord | AssignDouble | IfElse

    fun Operation(status: Boolean): Boolean {
        if(last?.symbol == DEC_STRING) {
            return AssignString(status)
        } else if(last?.symbol == DEC_INT) {
            return AssignInt(status)
        } else if(last?.symbol == DEC_COORD) {
            return AssignCoord(status)
        } else if(last?.symbol == IF) {
            return IfElse(status)
        } else if(last?.symbol == DEC_DOUBLE) {
            return AssignDouble(status)
        }
        return false
    }

    // DoubleExpr = int DoubleExpr' | var DoubleExpr' | double DoubleExpr'
    // DoubleExpr' = + DoubleExpr | - DoubleExpr | e

    fun DoubleExpr(): Pair<Boolean, Double?> {
        if (last?.symbol == INT) {
            val intValue = last?.lexeme?.toDouble()
            recognizeTerminal(INT)
            if (intValue != null) {
                return DoubleExprPrime(intValue)
            }
        } else if (last?.symbol == VAR) {
            val stringValue = last?.lexeme
            recognizeTerminal(VAR)
            var doubleFoundValue = insideVariableDoubleMap[stringValue]
            if (doubleFoundValue == null) {
                doubleFoundValue = variableDoubleMap[stringValue]
            }
            if (doubleFoundValue != null) {
                return DoubleExprPrime(doubleFoundValue)
            }
            var foundValue = insideVariableIntMap[stringValue]
            if (foundValue == null) {
                foundValue = variableIntMap[stringValue]
            }
            if (foundValue != null) {
                return DoubleExprPrime(foundValue.toDouble())
            }
        } else if (last?.symbol == DOUBLE) {
            val doubleValue = last?.lexeme?.toDouble()
            recognizeTerminal(DOUBLE)
            if (doubleValue != null) {
                return DoubleExprPrime(doubleValue)
            }
        }
        return Pair(false, null)
    }

    fun DoubleExprPrime(inValue: Double): Pair<Boolean, Double?> {
        if (last?.symbol == PLUS) {
            recognizeTerminal(PLUS)
            val result = DoubleExpr()
            if (result.first) {
                val computedValue = inValue + result.second!!
                return Pair(true, computedValue)
            }
        } else if (last?.symbol == MINUS) {
            recognizeTerminal(MINUS)
            val result = DoubleExpr()
            if (result.first) {
                val computedValue = inValue - result.second!!
                return Pair(true, computedValue)
            }
        }
        return Pair(true, inValue)
    }

    //InsideOperations ::= InsideOperation InsideOperations

    fun InsideOperations(status: Boolean): Boolean {
        if (InsideOperation(status) && InsideOperations(status))
            return true
        else return true
    }

    // InsideOperation ::= InsideAssignString | InsideAssignInt | InsideAssignCoord

    fun InsideOperation(status: Boolean): Boolean {
        if(last?.symbol == DEC_STRING) {
            return InsideAssignString(status)
        } else if(last?.symbol == DEC_INT) {
            return InsideAssignInt(status)
        } else if(last?.symbol == DEC_COORD) {
            return InsideAssignCoord(status)
        } else if(last?.symbol == IF) {
            return InsideIfElse(status)
        } else if(last?.symbol == DEC_DOUBLE) {
            return InsideAssignDouble(status)
        }
        return false
    }

    // MandatoryElementsList ::= MandatoryElements MandatoryElements'

    fun MandatoryElementsList(): Boolean {
        return MandatoryElements() && MandatoryElementsPrime()
    }

    // MandatoryElements ::= Streets Institutions

    fun MandatoryElements(): Boolean {
        if(last?.symbol == STREET) {
            return Streets()
        } else if (last?.symbol == INSTITUTION) {
            return Institutions()
        }  else return false
    }

    // MandatoryElements' ::= MandatoryElementsList | e

    fun MandatoryElementsPrime(): Boolean {
        if (MandatoryElementsList())
            return true
        else return true
    }

    // AdditionalElementsList ::= AdditionalElements AdditionalElementsList
    fun AdditionalElementsList(): Boolean {
        if(AdditionalElements() && AdditionalElementsList())
            return true
        else return true
    }

    // AdditionalElements ::= Squares | Statues | Lakes
    fun AdditionalElements(): Boolean {
        if (last?.symbol == SQUARE) {
            return Squares()
        } else if (last?.symbol == STATUE) {
            return Statues()
        } else if (last?.symbol == LAKE) {
            return Lakes()
        } else return false
    }
    // IfElse ::=  if ( IntExpr Compare IntExpr ) { Operations } IfElse'
    // Compare ::= == | != | < | >
    // IfElse' ::= else { Operations } | e
    fun IfElse(status: Boolean): Boolean {
        if (recognizeTerminal(IF) && recognizeTerminal(LPAREN)) {
            var firstVal = DoubleExpr()
            if (firstVal.first) {
                if (recognizeTerminal(EQUALS)) {
                    var secondValue = DoubleExpr()
                    if (secondValue.first) {
                        if (recognizeTerminal(RPAREN) && firstVal.second != null && secondValue.second != null && recognizeTerminal(LCPAREN)) {
                            if (firstVal.second == secondValue.second) {
                                if (Operations(true) && recognizeTerminal(RCPAREN)) {
                                    if (!status) return true
                                    if (recognizeTerminal(ELSE) && recognizeTerminal(LCPAREN) && Operations(false) && recognizeTerminal(
                                            RCPAREN))
                                        return true
                                    return true
                                }
                            } else if (firstVal.second != secondValue.second) {
                                if (Operations(false) && recognizeTerminal(RCPAREN)) {
                                    if (!status) return true
                                    if (recognizeTerminal(ELSE) && recognizeTerminal(LCPAREN) && Operations(true) && recognizeTerminal(
                                            RCPAREN))
                                        return true
                                    return true
                                }
                            }
                        }
                    }
                } else if (recognizeTerminal(NOTEQUAL)) {
                    var secondValue = DoubleExpr()
                    if (secondValue.first) {
                        if (recognizeTerminal(RPAREN) && firstVal.second != null && secondValue.second != null && recognizeTerminal(LCPAREN)) {
                            if (firstVal.second != secondValue.second) {
                                if (Operations(true) && recognizeTerminal(RCPAREN)) {
                                    if (!status) return true
                                    if (recognizeTerminal(ELSE) && recognizeTerminal(LCPAREN) && Operations(false) && recognizeTerminal(
                                            RCPAREN))
                                        return true
                                    return true
                                }
                            } else if (firstVal.second == secondValue.second) {
                                if (Operations(false) && recognizeTerminal(RCPAREN)) {
                                    if (!status) return true
                                    if (recognizeTerminal(ELSE) && recognizeTerminal(LCPAREN) && Operations(true) && recognizeTerminal(
                                            RCPAREN))
                                        return true
                                    return true
                                }
                            }
                        }
                    }
                } else if (recognizeTerminal(SMALLER)) {
                    var secondValue = DoubleExpr()
                    if (secondValue.first) {
                        if (recognizeTerminal(RPAREN) && firstVal.second != null && secondValue.second != null && recognizeTerminal(LCPAREN)) {
                            if (firstVal.second!! < secondValue.second!!) {
                                if (Operations(true) && recognizeTerminal(RCPAREN)) {
                                    if (!status) return true
                                    if (recognizeTerminal(ELSE) && recognizeTerminal(LCPAREN) && Operations(false) && recognizeTerminal(
                                            RCPAREN))
                                        return true
                                    return true
                                }
                            } else {
                                if (Operations(false) && recognizeTerminal(RCPAREN)) {
                                    if (!status) return true
                                    if (recognizeTerminal(ELSE) && recognizeTerminal(LCPAREN) && Operations(true) && recognizeTerminal(
                                            RCPAREN))
                                        return true
                                    return true
                                }
                            }
                        }
                    }
                } else if (recognizeTerminal(BIGGER)) {
                    var secondValue = DoubleExpr()
                    if (secondValue.first) {
                        if (recognizeTerminal(RPAREN) && firstVal.second != null && secondValue.second != null && recognizeTerminal(LCPAREN)) {
                            if (firstVal.second!! > secondValue.second!!) {
                                if (Operations(true) && recognizeTerminal(RCPAREN)) {
                                    if (!status) return true
                                    if (recognizeTerminal(ELSE) && recognizeTerminal(LCPAREN) && Operations(false) && recognizeTerminal(
                                            RCPAREN))
                                        return true
                                    return true
                                }
                            } else {
                                if (Operations(false) && recognizeTerminal(RCPAREN)) {
                                    if (!status) return true
                                    if (recognizeTerminal(ELSE) && recognizeTerminal(LCPAREN) && Operations(true) && recognizeTerminal(
                                            RCPAREN))
                                        return true
                                    return true
                                }
                            }
                        }
                    }
                }
            }
        }
        return false
    }

    fun Compare(): Boolean {
        if (recognizeTerminal(EQUALS)) return true
        else if (recognizeTerminal(BIGGER)) return true
        else if (recognizeTerminal(SMALLER)) return true
        else return (recognizeTerminal(NOTEQUAL))
    }

    // Streets ::= Street StreetPrime

    fun Streets(): Boolean {
        return Street() && StreetPrime()
    }

    // Street ::= street string { InsideOperations Bend InsideOperations Line }
    fun Street(): Boolean {
        if (recognizeTerminal(STREET)) {
            if (last?.symbol == STRING) {
                var name = last?.lexeme
                name = name!!.substring(1, name.length - 1)
                recognizeTerminal(STRING)
                if (recognizeTerminal(LCPAREN) && InsideOperations(true)) {
                    val bend = Bend()
                    if (bend.first) {
                        var coords = bend.second
                        val angle = bend.third
                        var bendCoordinates = generateGeoJSONCurve(Pair(coords!![0], coords!![1]), angle!!)
                        val line = Line()
                        if (line.first) {
                            val firstLine = line.second
                            val secondLine = line.third
                            if (recognizeTerminal(RCPAREN)) {

                                val markerFirstLine = MapRasterTiles.getPixelPosition(
                                    firstLine!!.longtitude,
                                    firstLine!!.latitude,
                                    ctx.beginTile.x,
                                    ctx.beginTile.y
                                )

                                val markerSecondLine = MapRasterTiles.getPixelPosition(
                                    secondLine!!.longtitude,
                                    secondLine!!.latitude,
                                    ctx.beginTile.x,
                                    ctx.beginTile.y
                                )

                                ctx.shapeRenderer.line(markerFirstLine.x, markerFirstLine.y, markerSecondLine.x, markerSecondLine.y)

                                val marker = MapRasterTiles.getPixelPosition(
                                    firstLine!!.longtitude,
                                    firstLine!!.latitude,
                                    ctx.beginTile.x,
                                    ctx.beginTile.y
                                )
                                geoJSON += "\n\t{\n" +
                                        "  \t\t\"type\": \"Feature\",\n" +
                                        "  \t\t\"properties\": {\n" +
                                        "\t\t\t\"element\": \"" + "Street" + "\",\n" +
                                        "\t\t\t\"name\": \"" + name + "\"\n" +
                                        "\t\t},\n" +
                                        "  \t\t\"geometry\": {\n" +
                                        "\t\t\t\"type\": \"LineString\",\n" +
                                        "\t\t\t\"coordinates\": [\n" +
                                        "        \t\t[" + (bendCoordinates?.get(0)!!.latitude) + ", " + (bendCoordinates?.get(0)!!.longtitude) + "],\n" +
                                        "        \t\t[" + (bendCoordinates?.get(1)!!.latitude) + ", " + (bendCoordinates?.get(1)!!.longtitude) + "],\n" +
                                        "        \t\t[" + (bendCoordinates?.get(2)!!.latitude) + ", " + (bendCoordinates?.get(2)!!.longtitude) + "],\n" +
                                        "        \t\t[" + (firstLine!!.latitude) + ", " + (firstLine!!.longtitude) + "],\n" +
                                        "        \t\t[" + (secondLine!!.latitude) + ", " + (secondLine!!.longtitude) + "]\n" +
                                        "    \t  ]\n" +
                                        "  \t\t}\n" +
                                        "\t  },\n"
                                insideVariableStringMap.clear()
                                insideVariableDoubleMap.clear()
                                insideVariableCoordPair.clear()
                                return true
                            }
                        }
                    }

                }
            }
        }
        return false
    }

    // SteeetPrime ::= Streets | e

    fun StreetPrime(): Boolean {
        if (Streets())
            return true
        else return true
    }

    // Institutions ::= Institution IntitutionPrime

    fun Institutions(): Boolean {
        return Institution() && InstitutionPrime()
    }

    // Institution ::= institution string { InsideOperations Address InsideOperations Events InsideOperations Block }

    fun Institution(): Boolean {
        if (recognizeTerminal(INSTITUTION)) {
            if (last?.symbol == STRING) {
                var name = last?.lexeme
                name = name!!.substring(1, name.length -1)
                recognizeTerminal(STRING)
                if (recognizeTerminal(LCPAREN) && InsideOperations(true)) {
                    var address = Address()
                    if (address.first) {
                        var ad = address.second
                        ad = ad!!.substring(1, ad.length - 1)
                        var events = Events()
                        if (events.first) {
                            var ev = events.second
                            var block = Block()
                            if (block.first) {
                                var list = block.second
                                if (recognizeTerminal(RCPAREN)) {

                                    val markerFirstLine = MapRasterTiles.getPixelPosition(
                                        list?.get(0)!!.longtitude,
                                        list?.get(0)!!.latitude,
                                        ctx.beginTile.x,
                                        ctx.beginTile.y
                                    )

                                    val markerSecondLine = MapRasterTiles.getPixelPosition(
                                        list?.get(1)!!.longtitude,
                                        list?.get(1)!!.latitude,
                                        ctx.beginTile.x,
                                        ctx.beginTile.y
                                    )

                                    val markerThirdLine = MapRasterTiles.getPixelPosition(
                                        list?.get(2)!!.longtitude,
                                        list?.get(2)!!.latitude,
                                        ctx.beginTile.x,
                                        ctx.beginTile.y
                                    )

                                    val markerFourthLine = MapRasterTiles.getPixelPosition(
                                        list?.get(3)!!.longtitude,
                                        list?.get(3)!!.latitude,
                                        ctx.beginTile.x,
                                        ctx.beginTile.y
                                    )

                                    ctx.shapeRenderer.circle(markerFirstLine.x, markerFirstLine.y, 10f)
                                    ctx.shapeRenderer.circle(markerSecondLine.x, markerSecondLine.y, 10f)
                                    ctx.shapeRenderer.circle(markerThirdLine.x, markerThirdLine.y, 10f)
                                    ctx.shapeRenderer.circle(markerFourthLine.x, markerFourthLine.y, 10f)

                                    geoJSON += "\n\t{\n" +
                                            "  \t\t\"type\": \"Feature\",\n" +
                                            "  \t\t\"properties\": {\n" +
                                            "\t\t\t\"element\": \"" + "Institution" + "\",\n" +
                                            "\t\t\t\"name\": \"" + name + "\",\n" +
                                            "\t\t\t\"address\": \"" + ad + "\",\n" +
                                            "\t\t\t\"events\":" + ev + "\n" +
                                            "\t\t},\n" +
                                            "  \t\t\"geometry\": {\n" +
                                            "\t\t\t\"type\": \"Polygon\",\n" +
                                            "\t\t\t\"coordinates\": [\n" +
                                            "      \t\t[\n" +
                                            "        \t\t[" + (list?.get(0)!!.latitude) + ", " + (list?.get(0)!!.longtitude) + "],\n" +
                                            "        \t\t[" + (list?.get(1)!!.latitude) + ", " + (list?.get(1)!!.longtitude) + "],\n" +
                                            "        \t\t[" + (list?.get(2)!!.latitude) + ", " + (list?.get(2)!!.longtitude) + "],\n" +
                                            "        \t\t[" + (list?.get(3)!!.latitude) + ", " + (list?.get(3)!!.longtitude) + "],\n" +
                                            "        \t\t[" + (list?.get(0)!!.latitude) + ", " + (list?.get(0)!!.longtitude) + "]\n" +
                                            "      \t\t]\n" +
                                            "    \t  ]\n" +
                                            "  \t\t}\n" +
                                            "\t  },\n"
                                    insideVariableStringMap.clear()
                                    insideVariableDoubleMap.clear()
                                    insideVariableCoordPair.clear()
                                    return true
                                }
                            }
                        }
                    }
                }
            }
        }
        return false
    }

    // Institution' ::= Institutions | e

    fun InstitutionPrime(): Boolean {
        if (Institutions())
            return true
        else return true
    }

    // Squares ::= Square Squares | e

    fun Squares(): Boolean {
        if (Square() && Squares())
            return true
        else return true
    }

    // Square ::= square string { InsideOperations Block }
    fun Square(): Boolean {
        if (recognizeTerminal(SQUARE)) {
            if (last?.symbol == STRING) {
                var name = last?.lexeme
                name = name!!.substring(1, name.length - 1)
                recognizeTerminal(STRING)
                if (recognizeTerminal(LCPAREN) && InsideOperations(true)) {
                    var block = Block()
                    if (block.first) {
                        if (recognizeTerminal(RCPAREN)) {
                            var list = block.second
                            geoJSON += "\n\t{\n" +
                                    "  \t\t\"type\": \"Feature\",\n" +
                                    "  \t\t\"properties\": {\n" +
                                    "\t\t\t\"element\": \"" + "Square" + "\",\n" +
                                    "\t\t\t\"name\": \"" + name + "\"\n" +
                                    "\t\t},\n" +
                                    "  \t\t\"geometry\": {\n" +
                                    "\t\t\t\"type\": \"Polygon\",\n" +
                                    "\t\t\t\"coordinates\": [\n" +
                                    "      \t\t[\n" +
                                    "        \t\t[" + (list?.get(0)!!.latitude) + ", " + (list?.get(0)!!.longtitude) + "],\n" +
                                    "        \t\t[" + (list?.get(1)!!.latitude) + ", " + (list?.get(1)!!.longtitude) + "],\n" +
                                    "        \t\t[" + (list?.get(2)!!.latitude) + ", " + (list?.get(2)!!.longtitude) + "],\n" +
                                    "        \t\t[" + (list?.get(3)!!.latitude) + ", " + (list?.get(3)!!.longtitude) + "],\n" +
                                    "        \t\t[" + (list?.get(0)!!.latitude) + ", " + (list?.get(0)!!.longtitude) + "]\n" +
                                    "      \t\t]\n" +
                                    "    \t  ]\n" +
                                    "  \t\t}\n" +
                                    "\t  },\n"
                            insideVariableStringMap.clear()
                            insideVariableDoubleMap.clear()
                            insideVariableCoordPair.clear()
                            return true
                        }
                    }
                }
            }
        }
        return false
    }

    // Statues ::= Statue Statues | e
    fun Statues(): Boolean {
        if (Statue() && Statues())
            return true
        else return true
    }

    // Statue ::= statue string { InsideOperations Point }
    fun Statue(): Boolean {
        if (recognizeTerminal(STATUE)) {
            if (last?.symbol == STRING) {
                var name = last?.lexeme
                name = name!!.substring(1, name.length - 1)
                recognizeTerminal(STRING)
                if (recognizeTerminal(LCPAREN) && InsideOperations(true)) {
                    var point = Point()
                    if (point.first && recognizeTerminal(RCPAREN)) {

                        geoJSON += "\n\t{\n" +
                                "  \t\t\"type\": \"Feature\",\n" +
                                "  \t\t\"properties\": {\n" +
                                "\t\t\t\"element\": \"" + "Statue" + "\",\n" +
                                "\t\t\t\"name\": \"" + name + "\"\n" +
                                "\t\t},\n" +
                                "  \t\t\"geometry\": {\n" +
                                "\t\t\t\"type\": \"Point\",\n" +
                                "\t\t\t\"coordinates\": [" + (point.second!!.latitude) + ", " + (point.second!!.longtitude) + "]\n" +
                                "  \t\t}\n" +
                                "\t  },\n"
                        insideVariableStringMap.clear()
                        insideVariableDoubleMap.clear()
                        insideVariableCoordPair.clear()
                        return true
                    }
                }
            }

            /*&& recognizeTerminal(STRING) && recognizeTerminal(LCPAREN) && InsideOperations(true) && Point() && recognizeTerminal(RCPAREN)) {
            insideVariableStringMap.clear()
            insideVariableDoubleMap.clear()
            insideVariableCoordPair.clear()
            return true */
        }
        return false
    }

    // Lakes ::= Lake Lakes | e
    fun Lakes(): Boolean {
        if (Lake() && Lakes())
            return true
        else return true
    }

    // Lake ::= lake string { InsideOperations Circle }
    fun Lake(): Boolean {
        if (recognizeTerminal(LAKE)) {
            if (last?.symbol == STRING) {
                var name = last?.lexeme
                name = name!!.substring(1, name.length -1)
                recognizeTerminal(STRING)
                if (recognizeTerminal(LCPAREN) && InsideOperations(true)) {
                    var circle = Circle()
                    if (circle.first) {
                        var coords = generateCirclePolygon(circle.second!!, circle.third!!, 20)
                        if (recognizeTerminal(RCPAREN)) {
                            geoJSON += "\n\t{\n" +
                                    "  \t\t\"type\": \"Feature\",\n" +
                                    "  \t\t\"properties\": {\n" +
                                    "\t\t\t\"element\": \"" + "Lake" + "\",\n" +
                                    "\t\t\t\"name\": \"" + name + "\"\n" +
                                    "\t\t},\n" +
                                    "  \t\t\"geometry\": {\n" +
                                    "\t\t\t\"type\": \"Polygon\",\n" +
                                    "\t\t\t\"coordinates\": [\n" +
                                    "      \t\t[\n" +
                                    "        \t\t[" + (coords?.get(0)!!.latitude) + ", " + (coords?.get(0)!!.longtitude) + "],\n" +
                                    "        \t\t[" + (coords?.get(1)!!.latitude) + ", " + (coords?.get(1)!!.longtitude) + "],\n" +
                                    "        \t\t[" + (coords?.get(2)!!.latitude) + ", " + (coords?.get(2)!!.longtitude) + "],\n" +
                                    "        \t\t[" + (coords?.get(3)!!.latitude) + ", " + (coords?.get(3)!!.longtitude) + "],\n" +
                                    "        \t\t[" + (coords?.get(4)!!.latitude) + ", " + (coords?.get(4)!!.longtitude) + "],\n" +
                                    "        \t\t[" + (coords?.get(5)!!.latitude) + ", " + (coords?.get(5)!!.longtitude) + "],\n" +
                                    "        \t\t[" + (coords?.get(6)!!.latitude) + ", " + (coords?.get(6)!!.longtitude) + "],\n" +
                                    "        \t\t[" + (coords?.get(7)!!.latitude) + ", " + (coords?.get(7)!!.longtitude) + "],\n" +
                                    "        \t\t[" + (coords?.get(8)!!.latitude) + ", " + (coords?.get(8)!!.longtitude) + "],\n" +
                                    "        \t\t[" + (coords?.get(9)!!.latitude) + ", " + (coords?.get(9)!!.longtitude) + "],\n" +
                                    "        \t\t[" + (coords?.get(0)!!.latitude) + ", " + (coords?.get(0)!!.longtitude) + "]\n" +
                                    "      \t\t]\n" +
                                    "    \t  ]\n" +
                                    "  \t\t}\n" +
                                    "\t  },\n"
                            insideVariableStringMap.clear()
                            insideVariableDoubleMap.clear()
                            insideVariableCoordPair.clear()
                            return true
                        }
                    }
                }
            }
        }
        return false
    }

    // Point ::= point Coordinate

    fun Point(): Pair<Boolean,Coordinate?> {
        if (recognizeTerminal(POINT)) {
            var coordinate = Coordinate()
            var cord = Coordinate("point", coordinate.second!!, coordinate.third!!)
            return Pair(coordinate.first, cord)
        }
        return Pair(false, null)
    }

    // Block ::= block ( Coordinate , Coordinate , Coordinate , Coordinate )
    fun Block(): Pair<Boolean, MutableList<Coordinate>?> {
        if (recognizeTerminal(BLOCK) && recognizeTerminal(LPAREN)) {
            var blockCoord: MutableList<Coordinate> = mutableListOf()
            var c1 = Coordinate()
            if (c1.first) {
                var newCoord = Coordinate("first", c1.second!!, c1.third!!)
                blockCoord.add(newCoord)
                if (recognizeTerminal(COMMA)) {
                    var c2 = Coordinate()
                    if (c2.first) {
                        newCoord = Coordinate("second", c2.second!!, c2.third!!)
                        blockCoord.add(newCoord)
                        if (recognizeTerminal(COMMA)) {
                            var c3 = Coordinate()
                            if (c3.first) {
                                newCoord = Coordinate("third", c3.second!!, c3.third!!)
                                blockCoord.add(newCoord)
                                if (recognizeTerminal(COMMA)) {
                                    var c4 = Coordinate()
                                    if (c4.first) {
                                        newCoord = Coordinate("four", c4.second!!, c4.third!!)
                                        blockCoord.add(newCoord)
                                        if (recognizeTerminal(RPAREN))
                                            return Pair(true, blockCoord)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return Pair(false, null)
    }

    // Bend ::= bend ( Coordinate , Coordinate, Angle )
    fun Bend(): Triple<Boolean, MutableList<Coordinate>?, Double?> {
        if (recognizeTerminal(BEND) && recognizeTerminal(LPAREN)) {
            var bendCoord: MutableList<Coordinate> = mutableListOf()
            var first = Coordinate()
            if (first.first) {
                var newCoord = Coordinate("bendFirst", first.second!!, first.third!!)
                bendCoord.add(newCoord)
                if (recognizeTerminal(COMMA)) {
                    var second = Coordinate()
                    if (second.first) {
                        newCoord = Coordinate("bendSecond", second.second!!, second.third!!)
                        bendCoord.add(newCoord)
                        if (recognizeTerminal(COMMA) && ((last?.symbol == INT) || (last?.symbol == DOUBLE) || (last?.symbol == VAR))) {
                            val an = Angle()
                            if (an.first) {
                                val angle = an.second
                                if (recognizeTerminal(RPAREN)) {
                                    return Triple(true, bendCoord, angle)
                                }
                            }
                        }
                    }
                }
            }
        }
        return Triple(false, null, null)
    }

    // Line ::= ( Coordinate , Coordinate )
    fun Line(): Triple<Boolean, Coordinate?, Coordinate?> {
        if (recognizeTerminal(LINE) && recognizeTerminal(LPAREN)) {
            var first = Coordinate()
            if (first.first) {
                val fCoord = Coordinate("first", first.second!!, first.third!!)
                if (recognizeTerminal(COMMA)) {
                    val second = Coordinate()
                    if (second.first) {
                        val sCoord = Coordinate("second", second.second!!, second.third!!)
                        if (recognizeTerminal(RPAREN)) {
                            return Triple(true, fCoord, sCoord)
                        }
                    }
                }
            }
        }
        return Triple(false, null, null)
    }

    // Circle ::= circle ( Coordinate, IntExpr )
    fun Circle(): Triple<Boolean, Coordinate?, Double?> {
        if (recognizeTerminal(CIRCLE) && recognizeTerminal(LPAREN)) {
            var coord = Coordinate()
            if (coord.first) {
                val coordinate = Coordinate("center", coord.second!!, coord.third!!)
                if (recognizeTerminal(COMMA) && ((last?.symbol == INT) || (last?.symbol == VAR) || (last?.symbol == DOUBLE))) {
                    var double = DoubleExpr()
                    if (double.first && recognizeTerminal(RPAREN)) {
                        return Triple(true, coordinate, double.second)
                    }
                }
            }
        }
        return Triple(false, null, null)
    }

    // Coordinate ::= ( Coordinate' | var
    // Coordinate' ::= IntExpr , Coordinate'' )
    // Coordinate'' ::= IntExpr | First | Second

    fun Coordinate(): Triple<Boolean, Double?, Double?> {
        if (recognizeTerminal(LPAREN)) {
            var expr = DoubleExpr()
            var f = First()
            var s = Second()
            if (expr.first) {
                var firstCoord = expr.second
                if(recognizeTerminal(COMMA)) {
                    var secExpr = DoubleExpr()
                    var first = First()
                    var second = Second()
                    if (firstCoord != null) {
                        if (secExpr.first) {
                            var secondCoord = secExpr.second
                            if (recognizeTerminal(RPAREN)) {
                                if ((secondCoord != null))
                                    return Triple(true, firstCoord, secondCoord)
                            }
                        } else if (first != null) {
                            if (recognizeTerminal(RPAREN))
                                return Triple(true, firstCoord, first)
                        } else if (second != null) {
                            if (recognizeTerminal(RPAREN))
                                return Triple(true, firstCoord, second)
                        }
                    }
                }
            } else if (f != null) {
                if (recognizeTerminal(COMMA)) {
                    var expression = DoubleExpr()
                    var fst = First()
                    var snd = Second()
                    if (expression.first) {
                        if (recognizeTerminal(RPAREN)) {
                            if (expression.second != null) {
                                return Triple(true, f, expression.second)
                            }
                        }
                    } else if (fst != null) {
                        if (recognizeTerminal(RPAREN))
                            return Triple(true, f, fst)
                    } else if (snd != null) {
                        if (recognizeTerminal(RPAREN))
                            return Triple(true, f, snd)
                    }
                }
            } else if (s != null) {
                if (recognizeTerminal(COMMA)) {
                    var expression = DoubleExpr()
                    var fst = First()
                    var snd = Second()
                    if (expression.first) {
                        if (recognizeTerminal(RPAREN)) {
                            if (expression.second != null) {
                                return Triple(true, s, expression.second)
                            }
                        }
                    } else if (fst != null) {
                        if (recognizeTerminal(RPAREN))
                            return Triple(true, s, fst)
                    } else if (snd != null) {
                        if (recognizeTerminal(RPAREN))
                            return Triple(true, s, snd)
                    }
                }
            }
        } else if (last?.symbol == VAR) {
            val stringValue = last?.lexeme
            recognizeTerminal(VAR)
            var coord = insideVariableCoordPair.find { it.name == stringValue }
            if (coord != null) {
                return Triple(true, coord.longtitude, coord.longtitude)
            }
            coord = variableCoordPair.find { it.name == stringValue }
            if (coord != null) {
                return Triple(true, coord.longtitude, coord.longtitude)
            }
        }
        return Triple(false, null, null)
    }

    // AssignString ::= dec_string var = AssignString'
    // AssignString' ::= string | var
    fun AssignString(status: Boolean): Boolean {
        if (recognizeTerminal(DEC_STRING) && last?.symbol == VAR) {
            val variableName = last?.lexeme
            recognizeTerminal(VAR)
            if (recognizeTerminal(ASSIGN) && (last?.symbol == STRING || last?.symbol == VAR)) {
                val stringValue = last?.lexeme
                recognizeTerminal(STRING)
                if (!status) return true
                val cleanedStringValue = stringValue?.removeSurrounding("\"")

                if (variableName != null && cleanedStringValue != null) {
                    variableStringMap[variableName] = cleanedStringValue
                    return true
                }
            }
        }
        return false
    }

    //AssignInt ::= dec_int var = AssignInt'
    //AssignInt' ::= int | var |

    fun AssignInt(status: Boolean): Boolean {
        if (recognizeTerminal(DEC_INT) && last?.symbol == VAR) {
            val variableName = last?.lexeme
            recognizeTerminal(VAR)
            if (recognizeTerminal(ASSIGN) && (last?.symbol == INT || last?.symbol == VAR)) {
                val intValue =  DoubleExpr().second
                if (!status) return true
                if (variableName != null && intValue != null) {
                    variableIntMap[variableName] = intValue.toInt()
                    return true
                }
            }
        }
        return false
    }

    // AssignCoord ::= dec_coord var = AssignCoord'
    // AssignCoord' ::= Coordinate | var

    fun AssignCoord(status: Boolean): Boolean {
        if (recognizeTerminal(DEC_COORD) && last?.symbol == VAR) {
            val variableName = last?.lexeme
            recognizeTerminal(VAR)
            if (recognizeTerminal(ASSIGN) && recognizeTerminal(LPAREN) && (last?.symbol == INT || last?.symbol == VAR)) {
                val leftIntValue =  DoubleExpr().second
                if(recognizeTerminal(COMMA)) {
                    val rightIntValue =  DoubleExpr().second
                    if (variableName != null && leftIntValue != null && rightIntValue != null) {
                        val newCoord = Coordinate(variableName, leftIntValue, rightIntValue)
                        if (recognizeTerminal(RPAREN)) {
                            if (!status) return true
                            variableCoordPair.add(newCoord)
                            return true
                        }
                    }
                }
            }
        }
        return false
    }

    //AssignDouble ::= dec_double var = AssignDouble'
    //AssignDouble' ::= double | var

    fun AssignDouble(status: Boolean): Boolean {
        if (recognizeTerminal(DEC_DOUBLE) && last?.symbol == VAR) {
            val variableName = last?.lexeme
            recognizeTerminal(VAR)
            if (recognizeTerminal(ASSIGN) && (last?.symbol == DOUBLE || last?.symbol == VAR)) {
                val intValue =  DoubleExpr().second
                if (!status) return true
                if (variableName != null && intValue != null) {
                    variableDoubleMap[variableName] = intValue
                    return true
                }
            }
        }
        return false
    }

    // InsideAssignString ::= dec_string var = InsideAssignString'
    // InsideAssignString' ::= string | var

    fun InsideAssignString(status: Boolean): Boolean {
        if (recognizeTerminal(DEC_STRING) && last?.symbol == VAR) {
            val variableName = last?.lexeme
            recognizeTerminal(VAR)
            if (recognizeTerminal(ASSIGN) && (last?.symbol == STRING || last?.symbol == VAR)) {
                val stringValue = last?.lexeme
                recognizeTerminal(STRING)
                if (!status) return true
                val cleanedStringValue = stringValue?.removeSurrounding("\"")

                if (variableName != null && cleanedStringValue != null) {
                    insideVariableStringMap[variableName] = cleanedStringValue
                    return true
                }
            }
        }
        return false
    }

    // InsideAssignInt ::= dec_int var = InsideAssignInt'
    // InsideAssignInt' ::= int | var

    fun InsideAssignInt(status: Boolean): Boolean {
        if (recognizeTerminal(DEC_INT) && last?.symbol == VAR) {
            val variableName = last?.lexeme
            recognizeTerminal(VAR)
            if (recognizeTerminal(ASSIGN) && (last?.symbol == INT || last?.symbol == VAR)) {
                val intValue =  DoubleExpr().second
                if (!status) return true
                if (variableName != null && intValue != null) {
                    insideVariableIntMap[variableName] = intValue.toInt()
                    return true
                }
            }
        }
        return false
    }
    // InsideAssignCoord ::= dec_coord var = InsideAssignCoord'
    // InsideAssignCoord' ::= Coordinate | var

    fun InsideAssignCoord(status: Boolean): Boolean {
        if (recognizeTerminal(DEC_COORD) && last?.symbol == VAR) {
            val variableName = last?.lexeme
            recognizeTerminal(VAR)
            if (recognizeTerminal(ASSIGN) && recognizeTerminal(LPAREN) && (last?.symbol == INT || last?.symbol == VAR)) {
                val leftIntValue =  DoubleExpr().second
                if(recognizeTerminal(COMMA)) {
                    val rightIntValue =  DoubleExpr().second
                    if (variableName != null && leftIntValue != null && rightIntValue != null) {
                        val newCoord = Coordinate(variableName, leftIntValue, rightIntValue)
                        if (recognizeTerminal(RPAREN)) {
                            if (!status) return true
                            insideVariableCoordPair.add(newCoord)
                            return true
                        }
                    }
                }
            }
        }
        return false
    }

    fun InsideAssignDouble(status: Boolean): Boolean {
        if (recognizeTerminal(DEC_DOUBLE) && last?.symbol == VAR) {
            val variableName = last?.lexeme
            recognizeTerminal(VAR)
            if (recognizeTerminal(ASSIGN) && (last?.symbol == DOUBLE || last?.symbol == VAR)) {
                val intValue =  DoubleExpr().second
                if (!status) return true
                if (variableName != null && intValue != null) {
                    insideVariableDoubleMap[variableName] = intValue
                    return true
                }
            }
        }
        return false
    }

    fun InsideIfElse(status: Boolean): Boolean {
        if (recognizeTerminal(IF) && recognizeTerminal(LPAREN)) {
            var firstVal = DoubleExpr()
            if (firstVal.first) {
                if (recognizeTerminal(EQUALS)) {
                    var secondValue = DoubleExpr()
                    if (secondValue.first) {
                        if (recognizeTerminal(RPAREN) && firstVal.second != null && secondValue.second != null && recognizeTerminal(LCPAREN)) {
                            if (firstVal.second == secondValue.second) {
                                if (InsideOperations(true) && recognizeTerminal(RCPAREN)) {
                                    if (!status) return true
                                    if (recognizeTerminal(ELSE) && recognizeTerminal(LCPAREN) && InsideOperations(false) && recognizeTerminal(
                                            RCPAREN))
                                        return true
                                    return true
                                }
                            } else if (firstVal.second != secondValue.second) {
                                if (InsideOperations(false) && recognizeTerminal(RCPAREN)) {
                                    if (!status) return true
                                    if (recognizeTerminal(ELSE) && recognizeTerminal(LCPAREN) && InsideOperations(true) && recognizeTerminal(
                                            RCPAREN))
                                        return true
                                    return true
                                }
                            }
                        }
                    }
                } else if (recognizeTerminal(NOTEQUAL)) {
                    var secondValue = DoubleExpr()
                    if (secondValue.first) {
                        if (recognizeTerminal(RPAREN) && firstVal.second != null && secondValue.second != null && recognizeTerminal(LCPAREN)) {
                            if (firstVal.second != secondValue.second) {
                                if (InsideOperations(true) && recognizeTerminal(RCPAREN)) {
                                    if (!status) return true
                                    if (recognizeTerminal(ELSE) && recognizeTerminal(LCPAREN) && InsideOperations(false) && recognizeTerminal(
                                            RCPAREN))
                                        return true
                                    return true
                                }
                            } else if (firstVal.second == secondValue.second) {
                                if (InsideOperations(false) && recognizeTerminal(RCPAREN)) {
                                    if (!status) return true
                                    if (recognizeTerminal(ELSE) && recognizeTerminal(LCPAREN) && InsideOperations(true) && recognizeTerminal(
                                            RCPAREN))
                                        return true
                                    return true
                                }
                            }
                        }
                    }
                } else if (recognizeTerminal(SMALLER)) {
                    var secondValue = DoubleExpr()
                    if (secondValue.first) {
                        if (recognizeTerminal(RPAREN) && firstVal.second != null && secondValue.second != null && recognizeTerminal(LCPAREN)) {
                            if (firstVal.second!! < secondValue.second!!) {
                                if (InsideOperations(true) && recognizeTerminal(RCPAREN)) {
                                    if (!status) return true
                                    if (recognizeTerminal(ELSE) && recognizeTerminal(LCPAREN) && InsideOperations(false) && recognizeTerminal(
                                            RCPAREN))
                                        return true
                                    return true
                                }
                            } else {
                                if (InsideOperations(false) && recognizeTerminal(RCPAREN)) {
                                    if (!status) return true
                                    if (recognizeTerminal(ELSE) && recognizeTerminal(LCPAREN) && InsideOperations(true) && recognizeTerminal(
                                            RCPAREN))
                                        return true
                                    return true
                                }
                            }
                        }
                    }
                } else if (recognizeTerminal(BIGGER)) {
                    var secondValue = DoubleExpr()
                    if (secondValue.first) {
                        if (recognizeTerminal(RPAREN) && firstVal.second != null && secondValue.second != null && recognizeTerminal(LCPAREN)) {
                            if (firstVal.second!! > secondValue.second!!) {
                                if (InsideOperations(true) && recognizeTerminal(RCPAREN)) {
                                    if (!status) return true
                                    if (recognizeTerminal(ELSE) && recognizeTerminal(LCPAREN) && InsideOperations(false) && recognizeTerminal(
                                            RCPAREN))
                                        return true
                                    return true
                                }
                            } else {
                                if (InsideOperations(false) && recognizeTerminal(RCPAREN)) {
                                    if (!status) return true
                                    if (recognizeTerminal(ELSE) && recognizeTerminal(LCPAREN) && InsideOperations(true) && recognizeTerminal(
                                            RCPAREN))
                                        return true
                                    return true
                                }
                            }
                        }
                    }
                }
            }
        }
        return false
    }

    // Angle ::= DoubleExpr | var

    private fun Angle(): Pair<Boolean, Double?> {
        var double = DoubleExpr()
        if (double.first) {
            return Pair(true, double.second)
        } else if (last?.symbol == VAR) {
            val stringValue = last?.lexeme
            recognizeTerminal(VAR)
            var value = variableDoubleMap[stringValue]
            return Pair(variableDoubleMap.keys.find { it == stringValue } != null, value)
        }
        return Pair(false, null)
    }

    // Address ::= address = Address'
    // Address' ::= string | var

    private fun Address(): Pair<Boolean, String?> {
        if (recognizeTerminal(ADDRESS) && recognizeTerminal(ASSIGN)) {
            if (last?.symbol == STRING) {
                var string = last?.lexeme
                recognizeTerminal(STRING)
                return Pair(true, string)
            } else if (last?.symbol == VAR) {
                val stringValue = last?.lexeme
                recognizeTerminal(VAR)
                var foundValue = insideVariableStringMap.keys.find { it == stringValue }
                if (foundValue == null) {
                    foundValue = variableStringMap.keys.find { it == stringValue }
                }
                return Pair(foundValue != null, foundValue)
            }
        }
        return Pair(false, null)
    }

    // Events ::= events = Events'
    // Events' ::= IntExpr | var
    private fun Events(): Pair<Boolean, Double?> {
        if (recognizeTerminal(EVENTS) && recognizeTerminal(ASSIGN)) {
            var expr = DoubleExpr()
            if (expr.first) {
                return Pair(true, expr.second)
            } else if (last?.symbol == VAR) {
                val stringValue = last?.lexeme
                var value : Double? = null
                recognizeTerminal(VAR)
                var foundValue = insideVariableDoubleMap.keys.find { it == stringValue }
                if (foundValue != null) value = insideVariableDoubleMap[foundValue]
                if (foundValue == null) {
                    foundValue = variableDoubleMap.keys.find { it == stringValue }
                    value = variableDoubleMap[foundValue]
                }
                return Pair(foundValue != null, value)
            }
        }
        return Pair(false, null)
    }

    // First ::= fst(var)

    private fun First(): Double? {
        if(recognizeTerminal(FST) && recognizeTerminal(LPAREN)) {
            if (last?.symbol == VAR) {
                val stringValue = last?.lexeme
                recognizeTerminal(VAR)
                var foundValue = insideVariableCoordPair.find { it.name == stringValue }
                if (foundValue == null) {
                    foundValue = variableCoordPair.find { it.name == stringValue }
                    recognizeTerminal(RPAREN)
                }
                if (foundValue != null)
                    return foundValue.longtitude
                else return null
            }
            return null
        }
        return null
    }

    // Second ::= snd(var)
    private fun Second(): Double? {
        if(recognizeTerminal(SND) && recognizeTerminal(LPAREN)) {
            if (last?.symbol == VAR) {
                val stringValue = last?.lexeme
                recognizeTerminal(VAR)
                var foundValue = insideVariableCoordPair.find { it.name == stringValue }
                if (foundValue == null) {
                    foundValue = variableCoordPair.find { it.name == stringValue }
                    recognizeTerminal(RPAREN)
                }
                if (foundValue != null)
                    return foundValue.longtitude
                else return null
            }
            return null
        }
        return null
    }

    fun generateCirclePolygon(center: Coordinate, radius: Double, numPoints: Int): List<Coordinate> {
        val coordinates = mutableListOf<Coordinate>()
        val angleIncrement = 4 * PI / numPoints

        for (i in 0 until numPoints) {
            val angle = i * angleIncrement
            val x = center.longtitude + radius * cos(angle)
            val y = center.latitude + radius * sin(angle)
            coordinates.add(Coordinate("coord", x, y))
        }

        return coordinates
    }

    fun generateGeoJSONCurve(coords: Pair<Coordinate, Coordinate>, angle: Double): List<Coordinate> {
        val start = coords.first
        val end = coords.second

        val angleRad = angle * PI / 180.0

        val distance = Math.hypot(end.longtitude - start.longtitude, end.latitude - start.latitude)

        val midX = (start.longtitude + end.longtitude) / 2.0
        val midY = (start.latitude + end.latitude) / 2.0

        val offset = distance * 0.1

        val control1X = midX + offset * cos(angleRad)
        val control1Y = midY + offset * sin(angleRad)

        val coordinates = mutableListOf<Coordinate>()
        coordinates.add(start)
        coordinates.add(Coordinate("coord", control1X, control1Y))
        coordinates.add(Coordinate("coord", control1X, control1Y))

        return coordinates
    }
    fun translateGeoJsonFromFile(filePath: String): String {
        val fileContent = File(filePath).readText()
        return translateGeoJson(fileContent)
    }
    fun translateGeoJson(input: String): String {
        var output = "city \"Maribor City 123\" {\n\n"
        val json = JSONObject(input)
        val features = json.getJSONArray("features")
        for (i in 0 until features.length()) {
            val feature = features.getJSONObject(i)
            val element = feature.getJSONObject("properties").getString("element")
            val name = feature.getJSONObject("properties").getString("name")
            output += "    $element \"$name\" {\n"
            if (element == "Institution") {
                val address = feature.getJSONObject("properties").getString("address")
                val events = feature.getJSONObject("properties").getDouble("events")
                output += "        address = \"$address\"\n"
                output += "        events = $events\n"
            }
            val geometryType = feature.getJSONObject("geometry").getString("type")
            val coordinates = feature.getJSONObject("geometry").getJSONArray("coordinates")
            when (geometryType) {
                "LineString" -> {
                    output += "        bend(${formatCoordinates(coordinates.getJSONArray(0))}, ${formatCoordinates(coordinates.getJSONArray(1))}, 1)\n"
                    output += "        line(${formatCoordinates(coordinates.getJSONArray(1))}, ${formatCoordinates(coordinates.getJSONArray(2))})\n"
                }
                "Polygon" -> {
                    output += "        block(${formatCoordinates(coordinates.getJSONArray(0))})\n"
                }
                "Point" -> {
                    output += "        point(${formatCoordinates(coordinates)})\n"
                }
                // Assume "Lake" is a circle
                else -> {
                    val center = coordinates.getJSONArray(0).getJSONArray(0)
                    output += "        circle(${formatCoordinates(center)}, 0.0005)\n"
                }
            }
            output += "    }\n\n"
        }
        output += "}\n"
        return output
    }

    fun formatCoordinates(coordinates: JSONArray): String {
        val coords = coordinates.joinToString(", ") { formatCoordinate(it) }
        return "($coords)"
    }

    fun formatCoordinate(coordinate: Any): String {
        return coordinate.toString()
    }

    private fun recognizeTerminal(value: Int) =
        if (last?.symbol == value) {
            last = scanner.getToken()
            true
        } else false

}

fun run(ctx: Context) {
    Parser(Scanner(ForForeachFFFAutomaton, File("test.txt").inputStream()), ctx).parse()
}
