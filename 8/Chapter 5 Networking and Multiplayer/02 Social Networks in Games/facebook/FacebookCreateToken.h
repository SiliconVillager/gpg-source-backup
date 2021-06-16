//         Facebook Demo for Game Programming Gems 8 by Claus Höfele          //
//==============================================================================

#ifndef WEB_FACEBOOK_CREATETOKEN_H
#define WEB_FACEBOOK_CREATETOKEN_H

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

/** Token used to identify an authentication request. */
typedef QString FacebookAuthenticationToken;

/** Request operator. */
struct FacebookCreateTokenRequest
{
  typedef void* Input;                        ///< Input type.
  enum { requiresSession = false };           ///< Session required flag.
  const static QString requestName;           ///< Request name.

  /**
   * Requests an authentication token.
   * @param input input parameters.
   * @param parameters parameters sent to the server.
   * @return status.
   */
  FacebookServiceState operator()(const Input& input, QVector<QString>& parameters);
};

/** Response operator. */
struct FacebookCreateTokenResponse
{
  typedef FacebookAuthenticationToken Output;  ///< Output type.

  /**
   * Parses response for an authentication token request.
   * @param document XML document that contains the response.
   * @param output parsed output.
   * @return status.
   */
  FacebookServiceState operator()(const QDomDocument& document, Output& output);
};

#endif // WEB_FACEBOOK_CREATETOKEN_H
