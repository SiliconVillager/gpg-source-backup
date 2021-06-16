//         Facebook Demo for Game Programming Gems 8 by Claus Höfele          //
//==============================================================================

#ifndef WEB_FACEBOOK_GETAPPUSERS_H
#define WEB_FACEBOOK_GETAPPUSERS_H

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
typedef QVector<QString> FacebookAppUsers;

/** Request operator. */
struct FacebookGetAppUsersRequest
{
  typedef void* Input;                        ///< Input type.
  enum { requiresSession = true };            ///< Session required flag.
  const static QString requestName;           ///< Request name.

  /**
   * Returns a list of friends associated with the currently authenticated user
   * that have also authorized this application.
   * @param input input parameters.
   * @param parameters parameters sent to the server.
   * @return status.
   */
  FacebookServiceState operator()(const Input& input, QVector<QString>& parameters);
};

/** Response operator. */
struct FacebookGetAppUsersResponse
{
  typedef FacebookAppUsers Output;            ///< Output type.

  /**
   * Parses response for app users request.
   * @param document XML document that contains the response.
   * @param output parsed output.
   * @return status.
   */
  FacebookServiceState operator()(const QDomDocument& document, Output& output);
};

#endif // WEB_FACEBOOK_GETAPPUSERS_H
