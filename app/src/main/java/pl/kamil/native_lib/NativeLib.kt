package pl.kamil.native_lib

class NativeLib {
    external fun encryptWithXOR(input: String, key: Int): String

    companion object {
        init {
            System.loadLibrary("notesproject")
        }
    }
}