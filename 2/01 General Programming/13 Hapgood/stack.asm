; Copyright (C) Bryon Hapgood, 2001. 
; All rights reserved worldwide.
;
; This software is provided "as is" without express or implied
; warranties. You may freely copy and compile this source into
; applications you distribute provided that the copyright text
; below is included in the resulting source code, for example:
; "Portions Copyright (C) Bryon Hapgood, 2001"
;
;  旼컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴�
;  � STACK.ASM													(C)2000 BpH. All rights reserved �
;  쳐컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴�
;  � 01/19/00	Started.																		 �
;  �																							 �
;  �																							 �
;  �																							 �
;  �																							 �
;  �																							 �
;  �																							 �
;  읕컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴�

	.586
	.model flat
	.code

	include stack.i

;	같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같
;	컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴
;   This enters function and saves the register context.
;	컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴
;	같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같

?SafeEnter@@YAXAAUSAFE@@ZZ:

						pop		edx	; return address
						mov		eax,[esp] ; safe
						;
						;
						;
						mov		[eax].safe.__ret,edx
						mov		[eax].safe.__ebx,ebx
						mov		[eax].safe.__ebp,ebp
						mov		[eax].safe.__esp,esp
						mov		[eax].safe.__esi,esi
						mov		[eax].safe.__edi,edi
						;
						;
						;
						pop		eax ; safe pointer
						pop		edx	; call function
						push	eax	; safe pointer
						mov		ebp,eax
						call	edx
						mov		eax,ebp
						jmp		sex

;	같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같
;	컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴
;   This function restores a register context thereby returning to who the context belongs to.
;	컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴컴
;	같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같같

?SafeExit@@YAXAAUSAFE@@@Z:

						pop		edx	;	return
						pop		eax	;	regs context
						;
						;
						;
sex:					mov		edi,[eax].safe.__edi
						mov		esi,[eax].safe.__esi
						mov		esp,[eax].safe.__esp
						mov		ebp,[eax].safe.__ebp
						mov		ebx,[eax].safe.__ebx
						mov		edx,[eax].safe.__ret
						mov		eax,[eax].safe.__eax
						;
						;
						;
						jmp		edx

						end
