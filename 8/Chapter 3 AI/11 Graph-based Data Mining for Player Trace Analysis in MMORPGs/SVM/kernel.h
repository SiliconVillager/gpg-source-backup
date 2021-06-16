/************************************************************************/
/*                                                                      */
/*   kernel.h                                                           */
/*                                                                      */
/*   User defined kernel function. Feel free to plug in your own.       */
/*                                                                      */
/*   Copyright: Thorsten Joachims                                       */
/*   Date: 16.12.97                                                     */
/*                                                                      */
/************************************************************************/

/* KERNEL_PARM is defined in svm_common.h The field 'custom' is reserved for */
/* parameters of the user defined kernel. You can also access and use */
/* the parameters of the other kernels. Just replace the line 
             return((double)(1.0)); 
   with your own kernel. */

  /* Example: The following computes the polynomial kernel. sprod_ss
              computes the inner product between two sparse vectors. 

      return((CFLOAT)pow(kernel_parm->coef_lin*sprod_ss(a->words,b->words)
             +kernel_parm->coef_const,(double)kernel_parm->poly_degree)); 
  */

/* If you are implementing a kernel that is not based on a
   feature/value representation, you might want to make use of the
   field "userdefined" in SVECTOR. By default, this field will contain
   whatever string you put behind a # sign in the example file. So, if
   a line in your training file looks like

   -1 1:3 5:6 #abcdefg

   then the SVECTOR field "words" will contain the vector 1:3 5:6, and
   "userdefined" will contain the string "abcdefg". */

#define SQUARE 0
#define UP 1
#define LEFT 2

double custom_kernel(KERNEL_PARM *kernel_parm, SVECTOR *aa, SVECTOR *bb) 
     /* plug in you favorite kernel */                          
{
	int *c;
	int *b;
	
	short int *x = (short int *) malloc(4000 * sizeof(short int));
	long m = 0;
	
	short int *y = (short int *) malloc(4000 * sizeof(short int));
	long n = 0;
	
	FILE *x_file;
	FILE *y_file;
	char line[80];
	int curr_vertex;
	
	long i, j; 
	float result = 0;

	//printf("-%s- vs -%s-\n", aa->userdefined, bb->userdefined); 
	
	x_file = fopen(aa->userdefined, "r");
	if (x_file == NULL)
		goto xx;
	else
		while(fgets(line, 80, x_file) != NULL)
	{
		sscanf (line, "%d", &curr_vertex);
		x[m] = curr_vertex;
		m++;
	}
	fclose(x_file);


	y_file = fopen(bb->userdefined, "r");
	if (y_file == NULL)
		goto xx;
	else
		while(fgets(line, 80, y_file) != NULL)
	{
		sscanf (line, "%d", &curr_vertex);
		y[n] = curr_vertex;
		n++;
	}
	fclose(y_file);

	m++;
	n++;

	c = (int *) malloc(m * n * sizeof(int));
	b = (int *) malloc(m * n * sizeof(int));



	for(i = 0; i < m; i++)
		c[i * n + 0] = 0;

	for(j = 0; j < n; j++)
		c[0 * n + j] = 0;

	for(i = 1; i < m; i++)
		for(j = 1; j < n; j++) {
			if (x[i-1] == y[j-1]) {
				c[(i * n) + j] = c[((i - 1) * n) + (j - 1)] + 1;
				b[(i * n) + j] = SQUARE;
			}
			else 
				if(c[((i - 1) * n) + j] >= c[(i * n) + (j - 1)]) {	
					c[(i * n) + j] = c[((i - 1) * n) +  j];
					b[(i * n) + j] = UP;
				}
				else {
					c[(i * n) + j] = c[(i * n) + (j - 1)];
					b[(i * n) + j] = LEFT;
				}
		}

	result = c[((m - 1) * n) + (n - 1) ];

	//printf("LCS = %f\n", result);
	result = (result * result)/(m * n);
	//printf("Kernel Value = %f\n", result);
	free(b);
	free(c);

	free(x);
	free(y);
xx:
	return result;
}
