//         Facebook Demo for Game Programming Gems 8 by Claus Höfele          //
//==============================================================================

#ifndef WEB_FACEBOOK_GETLOGGEDINUSER_H
#define WEB_FACEBOOK_GETLOGGEDINUSER_H

#include "FacebookServiceStates.h"

#include <QString>
#include <QVector>

//==============================================================================
// Forward Declarations
//==============================================================================

class QDomDocument;

//==============================================================================
// Declarations
//==============================================================================

/** User ID. */
typedef QString FacebookUid;

/** Request operator. */
struct FacebookGetLoggedInUserRequest
{
  typedef void* Input;                          ///< Input type.
  enum { requiresSession = true };              ///< Session required flag.
  const static QString requestName;             ///< Request name.

  /**
   * Returns the user associated with the current session.
   * @param input input parameters.
   * @param parameters parameters sent to the server.
   * @return status.
   */
  FacebookServiceState operator()(const Input& input, QVector<QString>& parameters);
};

/** Response operator. */
struct FacebookGetLoggedInUserResponse
{
  typedef FacebookUid Output;                   ///< Output type.

  /**
   * Parses response for logged in user.
   * @param document XML document that contains the response.
   * @param output parsed output.
   * @return status.
   */
  FacebookServiceState operator()(const QDomDocument& document, Output& output);
};

#endif // WEB_FACEBOOK_GETLOGGEDINUSER_H
