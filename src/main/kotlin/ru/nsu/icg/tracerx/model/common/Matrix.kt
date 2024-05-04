package ru.nsu.icg.tracerx.model.common

class Matrix(
    private val matrix: FloatArray,
    private val n: Int
) {
    companion object {
        fun of(vararg rows: FloatArray): Matrix {
            val n = rows.size
            require(n > 0) { "invalid matrix size: $n" }
            for (row in rows) {
                require (row.size == n) { "invalid row dimension: expected: $n, got ${rows[0].size}" }
            }
            val matrix = FloatArray(n * n)
            var index = 0
            for (row in rows) {
                for (num in row) {
                    matrix[index++] = num
                }
            }
            return Matrix(matrix, n)
        }

        fun eye(n: Int): Matrix {
            val matrix = FloatArray(n * n)
            for (i in 0..<n) {
                matrix[i * n + i] = 1f
            }
            return Matrix(matrix, n)
        }
    }

    operator fun get(line: Int, column: Int) = matrix[n * line + column]

    operator fun set(line: Int, column: Int, value: Float) {
        matrix[n * line + column] = value
    }

    operator fun times(scalar: Float) : Matrix {
        val newArr = FloatArray(n * n)
        for (i in newArr.indices) {
            newArr[i] = matrix[i] * scalar
        }
        return Matrix(newArr, n)
    }

    operator fun times(vector: Vector3D): Vector3D {
        require(n == 4) { "invalid dimensions: $n vs 3" }
        val x = this[0, 0] * vector.x + this[0, 1] * vector.y + this[0, 2] * vector.z + this[0, 3] * vector.w
        val y = this[1, 0] * vector.x + this[1, 1] * vector.y + this[1, 2] * vector.z + this[1, 3] * vector.w
        val z = this[2, 0] * vector.x + this[2, 1] * vector.y + this[2, 2] * vector.z + this[2, 3] * vector.w
        val w = this[3, 0] * vector.x + this[3, 1] * vector.y + this[3, 2] * vector.z + this[3, 3] * vector.w
        return Vector3D(x, y, z, w)
    }

    operator fun times(other: Matrix) : Matrix {
        require(n == other.n) { "invalid dimensions: ($n, $n) vs (${other.n}, ${other.n})" }
        val newMatrix = FloatArray(n * n)
        val result = Matrix(newMatrix, n)
        for (i in 0..<n) {
            for (j in 0..<other.n) {
                var s = 0f
                for (k in 0..<n) {
                    s += this[i, k] * other[k, j]
                }
                result[i, j] = s
            }
        }
        return result
    }

    override fun toString(): String {
        val builder = StringBuilder("[\n")
        for (i in 0..<n) {
            builder.append(matrix.copyOfRange(i * n, i * n + n).contentToString())
            builder.append(",\n")
        }
        builder.append("]")
        return builder.toString()
    }
}