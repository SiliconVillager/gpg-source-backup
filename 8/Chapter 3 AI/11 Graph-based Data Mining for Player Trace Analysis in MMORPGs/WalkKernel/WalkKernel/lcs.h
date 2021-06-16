#ifndef LCS_H
#define LCS_H

namespace LCS {

	using std::cerr;

	template <typename T>
	class Matrix {
	public:
		void init(int rows, int columns);
		T& operator()(int row, int column);
		void finalize();
	private:
		T *data;
		int max_rows;
		int max_columns;
	};

	template<typename T>
	void Matrix<T>::init(int rows, int columns) {
		max_rows = rows;
		max_columns = columns;
		data = new T[max_rows * max_columns];
	}

	template<typename T>
	T& Matrix<T>::operator()(int row, int column) {
		if((row * max_columns + column)  < (max_rows * max_columns)) {
			return data[row * max_columns + column];
		}
	}

	template<typename T>
	void Matrix<T>::finalize() {
		delete data;
	}

	enum DIRECTION {
		LEFT,
		UP,
		SQUARE
	};

	template <typename T>
	class lcs {
	public:
		void init(const T& x, const T& y);
		int compute(const T& x, const T& y);
		void print_lcs(const T& x);
		void print_recurse(const T& x, int i, int j);
		void finalize();
	private:
		Matrix<DIRECTION> b;
		Matrix<int> c;
		int m;
		int n;
	};

	template <typename T>
	void lcs<T>::init(const T& x, const T& y) {
		m = x.size() + 1;
		n = y.size() + 1;
		b.init(m,n);
		c.init(m,n);
	}

	template <typename T>
	int lcs<T>::compute(const T& x, const T& y) {

		for(int i = 0; i < m; i++)
			c(i, 0) = 0;
		for(int j = 0; j < n; j++)
			c(0, j) = 0;
		for(int i = 1; i < m; i++)
			for(int j = 1; j < n; j++) {
				if (x[i-1] == y[j-1]) {
					c(i,j) = c(i - 1, j - 1) + 1;
					b(i, j) = SQUARE;
				}
				else
					if(c(i - 1, j) >= c(i, j - 1)) {
						c(i, j) = c(i - 1, j);
						b(i,j) = UP;
					}
					else {
						c(i, j) = c(i, j - 1);
						b(i, j) = LEFT;
					}
			}
			return c(m-1,n-1);
	}

	template<typename T>
	void lcs<T>::print_lcs(const T& x) {
		print_recurse(x,m-1,n-1);
	}

	template<typename T>
	void lcs<T>::print_recurse(const T &x, int i, int j) {
		if((i == 0) || (j == 0))
			return;
		if (b(i,j) == SQUARE) {
			print_recurse(x, i - 1, j - 1);
			cerr << x[i-1] << " ";
		}
		else
			if (b(i,j) == UP)
				print_recurse(x, i - 1, j);
			else
				print_recurse(x, i, j - 1);
	}

	template<typename T>
	void lcs<T>::finalize() {
		b.finalize();
		c.finalize();
	}
}
#endif //LCS_H