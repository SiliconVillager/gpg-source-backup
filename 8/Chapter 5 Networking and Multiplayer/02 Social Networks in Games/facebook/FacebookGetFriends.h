//         Facebook Demo for Game Programming Gems 8 by Claus Höfele          //
//==============================================================================

#ifndef WEB_FACEBOOK_GETFRIENDS_H
#define WEB_FACEBOOK_GETFRIENDS_H

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

/** List of Facebook user IDs. */
typedef QVector<QString> FacebookFriends;

/** Request operator. */
struct FacebookGetFriendsRequest
{
  typedef void* Input;                      ///< Input type.
  enum { requiresSession = true };          ///< Session required flag.
  const static QString requestName;         ///< Request name.

  /**
   * Returns a list of friends associated with the currently authenticated user.
   * @param input input parameters.
   * @param parameters parameters sent to the server.
   * @return status.
   */
  FacebookServiceState operator()(const Input& input, QVector<QString>& parameters);
};

/** Response operator. */
struct FacebookGetFriendsResponse
{
  typedef FacebookFriends Output;           ///< Output type.

  /**
   * Parses response for
   * @param document XML document that contains the response.
   * @param output parsed output.
   * @return status.
   */
  FacebookServiceState operator()(const QDomDocument& document, Output& output);
};

#endif // WEB_FACEBOOK_GETFRIENDS_H
