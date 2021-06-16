// **********************************************************************************************************************
// 
// File:	    	BlockAllocation.cpp
// Created:	        30/08/2009
// Author:    		Michael Dailly
// Project:		    TemplateExample_V1
// Description:   	Shows a simple way to block allocate an array of objects in linear time.
//					This is NOT thread safe as the allocated list must be used before any other allocations can take place.
//					However in the case of systems like particle etc. this is usually acceptable as it avoids thousands of
//					individual allocations!
//
//					While block freeing is much slower with the stack, its hardly ever required. Systems like particles
//					are usually block allocated (an explosion needing 5,000 particles for example), but they are normally freed individually 
//					as each particle dies. Only on QUIT() does a system normally do block freeing and it's then not time criticle.
//					Also, to do block freeing efficiently with the linked list, you should also hold a LAST pointer, but this
//					would also slow down general allocation, particularly of single items.
//
// 
// Date				Version		BY		Comment
// ----------------------------------------------------------------------------------------------------------------------
// 19/09/2009		V1.0		MJD		1st version
// 
// **********************************************************************************************************************
#include	<tchar.h>
#include	<assert.h>


// Linked list example
class	LinkedClass
{
public:
	LinkedClass*	m_pNext;

	int			m_value1;
	void*		m_pData;
	int			m_MoreValues;
};


// Stack example
class	StackClass
{
public:
	int			m_value1;
	void*		m_pData;
	int			m_MoreValues;
};




#define	BLOCK_ALLOC			5000					// number of items to block allocate
#define	MAX_OBJECTS			32000					// 32,000 objects to allocate from

	// Stack allocator
	int				g_SP;
	StackClass*		pStack[MAX_OBJECTS];
	StackClass		ObjectStack[MAX_OBJECTS];

	// Linked List Allocator
	LinkedClass*	pFirst;
	LinkedClass*	pObjectList;

	


// #############################################################################################
/// Function:<summary>
///             Allocate a BLOCK of objects
///          </summary>
///
/// In:		 <param name="_count">Number of objects to try and allocate</param>
/// Out:	 <returns>
///				A pointer to the base of an array holding _count number of objects.
///				_count is modified to hold the actual number allocated (if there wasn't enough).
///				NOTE: if there were NO objects free, count is 0 and the pointer returned is meaningless.
///			 </returns>
// #############################################################################################
StackClass**	StackBlockAlloc(int& _count)
{
	assert(_count>0);

	if( g_SP<_count ) _count = g_SP;
	g_SP -= _count;
	return &(pStack[g_SP]);
}


// #############################################################################################
/// Function:<summary>
///             Push a single object onto the stack
///          </summary>
///
/// In:		 <param name="_obj">Obejct to free</param>
// #############################################################################################
void Push( StackClass* _obj)
{
	pStack[g_SP++] = _obj;
}


// #############################################################################################
/// Function:<summary>
///              Pop a free object from the stack
///          </summary>
///
/// Out:	 <returns>
///				A free object or NULL
///			 </returns>
// #############################################################################################
StackClass* Pop( void )
{

	if(g_SP==0) return NULL;
	return pStack[--g_SP];
}


// #############################################################################################
/// Function:<summary>
///             Free a value with using the linked list
///          </summary>
///
/// In:		 <param name="_obj">Object to add to free list</param>
// #############################################################################################
void	Free( LinkedClass* _obj )
{
	_obj->m_pNext = pFirst;
	pFirst = _obj;
}


// #############################################################################################
/// Function:<summary>
///             Allocate a free object
///          </summary>
///
/// Out:	 <returns>
///				the free object or NULL
///			 </returns>
// #############################################################################################
LinkedClass*	Alloc( void )
{
	LinkedClass* pObj = pFirst;
	if( pObj == NULL ) return NULL;

	pFirst = pObj->m_pNext;
	pObj->m_pNext = NULL;
	return pObj;
}


// #############################################################################################
/// Function:<summary>
///             Allocate a chain of objects based on "_count"
///          </summary>
///
/// In:		 <param name="_count">Number of objects to try and allocate</param>
/// Out:	 <returns>
///				_count is set to the actual number of elemets allocated
///				a linked list of "_count" items 
///			 </returns>
// #############################################################################################
LinkedClass*	LinkedBlockAlloc( int& _count )
{
	if( pFirst==NULL ){
		_count = 0;
		return NULL;
	}
	
	int counter = 0;

	// Loop through the chain for "_count" objects, and then return that chain
	LinkedClass* pLast;
	LinkedClass* pCurrent = pFirst;
	while(_count!=0)
	{
		++counter;
		pLast = pCurrent;
		pCurrent = pCurrent->m_pNext;
		if( pCurrent==NULL ){
			break;
		}
		--_count;
	}

	// Set the number we actually managed and unlink the chain.
	_count = counter;
	pCurrent = pFirst;
	pFirst = pLast->m_pNext;
	pLast->m_pNext = NULL;
	return pCurrent;
}


// #############################################################################################
/// Function:<summary>
///             Initialise both allocation methods.
///          </summary>
// #############################################################################################
void	Init( void )
{
	// Init the stack
	g_SP = 0;
	for(int i=0;i<MAX_OBJECTS;i++){
		ObjectStack[i].m_MoreValues = (int) (i^0xffffffff);		// Put some Data in there so we can see each object
		ObjectStack[i].m_value1 = i;							//
		ObjectStack[i].m_pData = (void*)0x12345678;				//
	
		Push( &ObjectStack[i] );								// and push it onto the free stack
	}


	// Linked List allocator
	pObjectList = new LinkedClass[MAX_OBJECTS]();
	pFirst = NULL;
	for(int i=0;i<(MAX_OBJECTS-1);i++){
		pObjectList[i].m_MoreValues = (int) (i^0xffffffff);		// Put some Data in there so we can see each object
		pObjectList[i].m_value1 = i;							//
		pObjectList[i].m_pData = (void*)0x12345678;				//
	
		Free( &pObjectList[i] );								// and link it in
	}
}


// #############################################################################################
/// Function:<summary>
///             Simple example core
///          </summary>
// #############################################################################################
int _tmain(int argc, _TCHAR* argv[])
{
	Init();


	while(true)
	{
		int lp;
		StackClass*  SFree[BLOCK_ALLOC];

		// Do block allocation from the Stack
		int scount = BLOCK_ALLOC;
		StackClass** pArray = StackBlockAlloc(scount);


		// Do block allocation from the Stack
		int lcount = BLOCK_ALLOC;
		LinkedClass* pList = LinkedBlockAlloc(lcount);



		// Due to the natrure of the stack block alloc, we need to take a 
		// copy of the array to be able to free it. Normally each subsystem 
		// would take a copy of the array as it processed it, so we would not 
		// normally require the copy.
		memcpy(SFree,pArray,scount*sizeof(StackClass*));



		// Free the block allocated objects one at a time.
		for(lp = 0;lp<BLOCK_ALLOC;lp++)
		{
			Push( SFree[lp] );
		}


		// Free the block allocated objects one at a time.
		while(pList!=NULL)
		{
			LinkedClass* pNext = pList->m_pNext;
			Free(pList);
			pList = pNext;
		}
	}

	return 0;
}



