package activity

import android.view.KeyEvent
import app.Task
import event.BarCodeScannerEvent
import org.greenrobot.eventbus.EventBus
import task.QueryVIPTask

class CharArrayBuffer(capacity: Int)
{
    private val mByteArrayBuffer = CharArray(capacity)
    private var nIndex = 0

    fun append(c: Char): CharArrayBuffer
    {
        mByteArrayBuffer[nIndex] = c
        nIndex ++
        return this
    }

    fun clear() { nIndex = 0 }

    override fun toString() = String(mByteArrayBuffer, 0, nIndex)
}

object BarCodeScanner
{                                               // 0    1   2    3    4    5    6    7    8     9
    private val ShiftKey = charArrayOf(')', '!', '@', '#', '$', '%', '^', '&', '*', '(')

    private val mCharBuffer = CharArrayBuffer(100)

    private var mCaps = false

    fun onScanner(env: KeyEvent)
    {
        val key = env.keyCode

        if (key == KeyEvent.KEYCODE_SHIFT_LEFT || key == KeyEvent.KEYCODE_SHIFT_RIGHT ||
                key == KeyEvent.KEYCODE_CAPS_LOCK) {
            mCaps = true
            return
        }

        if (key == KeyEvent.KEYCODE_ENTER || key == KeyEvent.KEYCODE_NUMPAD_ENTER)
        {
            val code = mCharBuffer.toString()
            Task.AsyncHandler.post(QueryVIPTask(code))
            mCharBuffer.clear()
            return
        }

        val c = getInputCode(env)
        mCaps = false
        if (c == ' ') {
            return
        }

        mCharBuffer.append( c )
    }

    private fun getInputCode(env: KeyEvent): Char
    {
        val key = env.keyCode

        return if (key >= KeyEvent.KEYCODE_A && key <= KeyEvent.KEYCODE_Z) {
            (if (mCaps) 'A' else 'a') + key - KeyEvent.KEYCODE_A
        } else if (key >= KeyEvent.KEYCODE_0 && key <= KeyEvent.KEYCODE_9) {
            val v = key - KeyEvent.KEYCODE_0
            if (mCaps) { ShiftKey[v] } else { '0' + v }
        } else {

            if (mCaps) {
                when (key)
                {
                    KeyEvent.KEYCODE_MINUS -> '_'
                    KeyEvent.KEYCODE_EQUALS -> '+'
                    KeyEvent.KEYCODE_LEFT_BRACKET -> '{'
                    KeyEvent.KEYCODE_RIGHT_BRACKET -> '}'
                    KeyEvent.KEYCODE_BACKSLASH -> '|'
                    KeyEvent.KEYCODE_SEMICOLON -> ':'
                    KeyEvent.KEYCODE_APOSTROPHE -> '"'
                    KeyEvent.KEYCODE_COMMA -> '<'
                    KeyEvent.KEYCODE_PERIOD -> '>'
                    KeyEvent.KEYCODE_SLASH -> '?'
                    else -> ' '
                }
            } else {
                when (key)
                {
                    KeyEvent.KEYCODE_MINUS -> '-'
                    KeyEvent.KEYCODE_EQUALS -> '='
                    KeyEvent.KEYCODE_LEFT_BRACKET -> '['
                    KeyEvent.KEYCODE_RIGHT_BRACKET -> ']'
                    KeyEvent.KEYCODE_BACKSLASH -> '\\'
                    KeyEvent.KEYCODE_SEMICOLON -> ';'
                    KeyEvent.KEYCODE_APOSTROPHE -> '\''
                    KeyEvent.KEYCODE_COMMA -> ','
                    KeyEvent.KEYCODE_PERIOD -> '.'
                    KeyEvent.KEYCODE_SLASH -> '/'
                    KeyEvent.KEYCODE_NUMPAD_SUBTRACT -> '-'
                    KeyEvent.KEYCODE_NUMPAD_ADD -> '+'
                    else -> ' '
                }
            }
        }
    }

}