package extracells.client.gui.widget

import net.minecraft.client.gui.{GuiScreen, FontRenderer, GuiTextField}
import org.lwjgl.input.Keyboard

class DigitTextField(fontRenderer: FontRenderer, x: Int, y: Int, l: Int, h: Int) extends GuiTextField(fontRenderer, x, y, l, h) {
  private def isWhitelisted(char: Char): Boolean =
    "0123456789".contains(char.toString)

  override def textboxKeyTyped(char: Char, key: Int): Boolean = {
    if (!this.isFocused)
      return false
    char match {
      case 0x1 =>
        this.setCursorPositionEnd()
        this.setSelectionPos(0)
        true
      case 0x3 =>
        GuiScreen.setClipboardString(this.getSelectedText)
        true
      case 0x16 =>
        this.writeText(GuiScreen.getClipboardString)
        true
      case 0x18 =>
        GuiScreen.setClipboardString(this.getSelectedText)
        this.writeText("")
        true
      case _ =>
        key match {
          case Keyboard.KEY_ESCAPE =>
            this.setFocused(false)
            true
          case 0xE =>
            if (GuiScreen.isCtrlKeyDown)
              this.deleteWords(-1)
            else
              this.deleteFromCursor(-1)
            true
          case 0xC7 =>
            if (GuiScreen.isShiftKeyDown)
              this.setSelectionPos(0)
            else
              this.setCursorPositionZero()
            true
          case 0xCB =>
            if (GuiScreen.isShiftKeyDown) {
              if (GuiScreen.isCtrlKeyDown)
                this.setSelectionPos(this.getNthWordFromPos(-1,this.getSelectionEnd))
              else
                this.setSelectionPos(this.getSelectionEnd - 1)
            }
            else if (GuiScreen.isCtrlKeyDown)
              this.setCursorPosition(this.getNthWordFromCursor(-1))
            else
              this.moveCursorBy(-1)
            true
          case 0xCD =>
            if (GuiScreen.isShiftKeyDown) {
              if (GuiScreen.isCtrlKeyDown)
                this.setSelectionPos(this.getNthWordFromPos(1,this.getSelectionEnd))
              else
                this.setSelectionPos(this.getSelectionEnd + 1)
            }
            else if (GuiScreen.isCtrlKeyDown)
              this.setCursorPosition(this.getNthWordFromCursor(1))
            else
              this.moveCursorBy(1)
            true
          case 0xCF =>
            if (GuiScreen.isShiftKeyDown)
              this.setSelectionPos(getText.length)
            else
              this.setCursorPositionEnd()
            true
          case 0xD3 =>
            if (GuiScreen.isCtrlKeyDown)
              this.deleteWords(1)
            else
              this.deleteFromCursor(1)
            true
          case _ =>
            if (isWhitelisted(char)) {
              this.writeText(char.toString)
              true
            }
            else if (char.equals('-') && this.getText.isEmpty) {
              writeText(char.toString)
              true
            }
            else
              false
        }
    }
  }
}
